package ie.ul.fitbook.ui.profile.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ie.ul.fitbook.R;
import ie.ul.fitbook.login.Login;
import ie.ul.fitbook.network.NetworkUtils;
import ie.ul.fitbook.profile.Profile;
import ie.ul.fitbook.ui.profile.ProfileCreationActivity;
import ie.ul.fitbook.ui.profile.viewmodels.ProfileViewModel;
import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.storage.Storage;
import ie.ul.fitbook.storage.Stores;
import ie.ul.fitbook.utils.Utils;

/**
 * The fragment for entering basic user details into
 */
public class BasicDetailsFragment extends Fragment {
    /**
     * The profile being edited
     */
    private Profile profile;
    /**
     * The image view with the profile image
     */
    private ImageView profileImage;
    /**
     * The name edit text field
     */
    private EditText nameField;
    /**
     * The city edit text field
     */
    private EditText cityField;
    /**
     * The state edit text field
     */
    private EditText stateField;
    /**
     * The spinner to choose favourite activity
     */
    private Spinner activityField;
    /**
     * A URI used for pointing to the chosen image
     */
    private Uri chosenPictureURI;
    /**
     * The chosen picture bitmap
     */
    private Bitmap chosenPictureBitmap;
    /**
     * The storage reference used for uploads
     */
    private StorageReference uploadReference;
    /**
     * The parent activity of this fragment
     */
    private FragmentActivity activity;
    /**
     * This flag indicates if we are editing our profile or not
     */
    private boolean editing;
    /**
     * Request code for capturing profile picture
     */
    private static final int PROFILE_PIC_CAPTURE = 123;
    /**
     * Saves an upload reference in progress
     */
    private static final String UPLOAD_REF = "ie.ul.fitbook.UPLOAD_REF";
    /**
     * Image uri that we saved if an image was specified
     */
    private static final String IMAGE_URI = "ie.ul.fitbook.IMAGE_URI";
    /**
     * Image bitmap that we saved if an image was specified
     */
    private static final String IMAGE_BITMAP = "ie.ul.fitbook.IMAGE_BITMAP";
    /**
     * The permissions required for this activity
     */
    private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.CAMERA};
    /**
     * Request code for requesting permissions
     */
    private static final int PERMISSION_REQUEST_CODE = 321;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basic_details, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.activity = requireActivity();

        ProfileCreationActivity activity;

        if (!(this.activity instanceof ProfileCreationActivity)) {
            throw new IllegalStateException("BasicDetailsFragment needs to be in the context of a ProfileCreationActivity");
        } else {
            activity = (ProfileCreationActivity)this.activity;
        }

        editing = activity.isInEditMode(); // check if the activity was launched with a request to edit

        setupProfile();

        String imageURI = null;
        if (savedInstanceState != null) {
            String uploadRef = savedInstanceState.getString(UPLOAD_REF);
            if (uploadRef != null)
                restoreUploads(uploadRef);

            imageURI = savedInstanceState.getString(IMAGE_URI);

            if (imageURI == null)
                chosenPictureBitmap = savedInstanceState.getParcelable(IMAGE_BITMAP);
        }

        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> activity.onCancel());

        Button next = view.findViewById(R.id.next);
        next.setOnClickListener(v -> onNext(view));

        Button upload = view.findViewById(R.id.upload);
        upload.setOnClickListener(v -> onUploadPressed());

        setupProfilePicture(view, imageURI);

        nameField = view.findViewById(R.id.nameTextField);
        cityField = view.findViewById(R.id.cityTextField);
        stateField = view.findViewById(R.id.stateTextField);
        activityField = view.findViewById(R.id.activityDropdown);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
            throw new IllegalStateException("You need to be logged in to create a profile");

        if (!editing) {
            nameField.setText(user.getDisplayName());
        }

        setUpActivitySpinner();
        fillFieldsWithProfile();
    }

    /**
     * Set up the profile instance being edited
     */
    private void setupProfile() {
        if (!editing) {
            ProfileViewModel profileViewModel = new ViewModelProvider(this.activity).get(ProfileViewModel.class);

            Profile profile = profileViewModel.getSelectedProfile().getValue();
            if (profile == null) {
                profile = new Profile();
                profileViewModel.selectProfile(profile);
            }

            this.profile = profile;
        } else {
            Profile loggedIn = Login.getProfile();

            if (loggedIn == null)
                throw new IllegalStateException("Login.getProfile() returned null, has the logged in user's profile been set?");

            this.profile = loggedIn;
        }
    }

    /**
     * This method sets up the displayed profile picture in this fragment
     * @param view the view passed in to onViewCreated
     * @param imageURI an Image URI that wa saved in the savedInstanceState
     */
    private void setupProfilePicture(View view, String imageURI) {
        profileImage = view.findViewById(R.id.profileImage);

        if (editing) {
            profileImage.setImageBitmap(profile.getProfileImage());
        } else {
            if (imageURI != null)
                setProfileImage(Uri.parse(imageURI));
            else if (chosenPictureBitmap != null)
                setProfileImage(chosenPictureBitmap);
            else
                profileImage.setImageResource(R.drawable.profile);
        }
    }

    /**
     * This method fills the fields with information from the logged in profile
     */
    private void fillFieldsWithProfile() {
        if (editing) {
            nameField.setText(profile.getName());
            cityField.setText(profile.getCity());
            stateField.setText(profile.getState());
            String activity = profile.getFavouriteSport();
            SpinnerAdapter adapter = activityField.getAdapter();

            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(activity)) {
                    activityField.setSelection(i);
                    break;
                }
            }
        }
    }


    /**
     * Restores any uploads that were in progress
     * @param uploadRef the upload URI string
     */
    private void restoreUploads(String uploadRef) {
        uploadReference = FirebaseStorage.getInstance().getReferenceFromUrl(uploadRef);

        List<UploadTask> tasks = uploadReference.getActiveUploadTasks();
        if (tasks.size() > 0) {
            UploadTask task = tasks.get(0); // there should be only one task running here
            task.addOnFailureListener(activity, e -> onUploadError());
        }
    }

    /**
     * This method sets up the values for the activity spinner
     */
    private void setUpActivitySpinner() {
        List<String> values = new ArrayList<>();

        for (Sport sport : Sport.values()) {
            String value = sport.toString();
            value = value.charAt(0) + value.substring(1).toLowerCase();

            values.add(value);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, values);
        activityField.setAdapter(arrayAdapter);
    }

    /**
     * Handles when the next button is clicked
     * @param view the view to navigate from
     */
    private void onNext(View view) {
        String name = nameField.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(activity, "You need to enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        String city = cityField.getText().toString();
        if (city.isEmpty()) {
            Toast.makeText(activity, "You need to enter a city", Toast.LENGTH_SHORT).show();
            return;
        }

        String state = stateField.getText().toString();
        if (state.isEmpty()) {
            Toast.makeText(activity, "You need to enter a state", Toast.LENGTH_SHORT).show();
            return;
        }

        String activityValue = (String)activityField.getSelectedItem();
        if (activityValue.isEmpty()) {
            Toast.makeText(activity, "You need to choose a favourite activity", Toast.LENGTH_SHORT).show();
            return;
        }

        profile.setName(name);
        profile.setCity(city);
        profile.setState(state);
        profile.setFavouriteSport(Sport.convertToSport(activityValue));

        Navigation.findNavController(view).navigate(R.id.action_basicDetailsFragment_to_biographyFragment);
    }

    /**
     * Displays a toast saying an error occurred during image picking
     */
    private void doImageError() {
        Toast.makeText(activity, "An error occurred setting the profile picture", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Retrieve the list of intents for choosing a camera
     * @return the camera to use
     */
    private List<Intent> getCameraIntents() {
        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

        for (ResolveInfo res : listCam) {
            String packageName = res.activityInfo.packageName;
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        return cameraIntents;
    }

    /**
     * Creates an intent for choosing an image from camera or filesystem
     * @return the intent to launch
     */
    private Intent getImageIntent() {
        List<Intent> cameraIntents = getCameraIntents();

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose image source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[0]));

        return chooserIntent;
    }

    /**
     * This method handles upload pressed
     */
    private void onUploadPressed() {
        if (!checkPermissions()) {
            requestPermission();
        } else {
            onUploadPermissionsGranted();
        }
    }

    /**
     * This callback is called when upload is requested and upload permissions were given
     */
    private void onUploadPermissionsGranted() {
        if (NetworkUtils.isNetworkConnected(activity)) { // we don't want a checker to see if connectivity is restored here since if the user connects to the internet and presses upload again, it will check again
            Intent imageIntent = getImageIntent();
            startActivityForResult(imageIntent, PROFILE_PIC_CAPTURE);
        } else {
            Toast.makeText(activity, "Cannot upload profile images when disconnected from the internet", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * The callback for when an error occurs
     */
    private void onUploadError() {
        doImageError();
        profileImage.setImageResource(R.drawable.profile);
    }

    /**
     * Uploads the image uri for profile photo
     * @param imageURI the uri of the chosen photo
     */
    private void processImageUri(Uri imageURI) {
        Storage userStorage = Storage.getInstance(Stores.USERS);

        if (userStorage != null) {
            StorageReference childReference = userStorage.getChildFolder(Profile.PROFILE_IMAGE_PATH);
            childReference.putFile(imageURI)
                    .addOnFailureListener(activity, e -> onUploadError());

            setProfileImage(imageURI);
        } else {
            doImageError();
        }
    }

    /**
     * Writes the byte array stream to disk
     * @param baos the stream to write
     */
    private void writeImageToDisk(ByteArrayOutputStream baos) {
        File file = Utils.getProfileImageLocation(activity);

        Toast toast = Toast.makeText(activity, "Error occurred saving profile picture to disk", Toast.LENGTH_SHORT);
        if (file == null) {
            toast.show();
        } else {
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                baos.writeTo(outputStream);
            } catch (IOException ex) {
                toast.show();
            }
        }
    }

    /**
     * Uploads the image by bitmap as profile photo
     * @param image the bitmap of the photo
     */
    private void processImageBitmap(Bitmap image) {
        Storage userStorage = Storage.getInstance(Stores.USERS);

        if (userStorage != null) {
            profileImage.setImageBitmap(image);
            //profileImage.setDrawingCacheEnabled(true);
            //Bitmap bitmap = profileImage.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            writeImageToDisk(baos);
            byte[] data = baos.toByteArray();;

            StorageReference childReference = userStorage.getChildFolder(Profile.PROFILE_IMAGE_PATH);
            childReference.putBytes(data)
                    .addOnFailureListener(activity, e -> onUploadError());
        } else {
            doImageError();
        }
    }

    /**
     * Sets the profile image from the URI
     * @param imageURI the URI of the image
     */
    private void setProfileImage(Uri imageURI) {
        try {
            Bitmap bitmap = Utils.getBitmapFromFile(activity, imageURI);
            setProfileImage(bitmap);
        } catch (Exception ex) {
            doImageError();
        }
    }

    /**
     * Sets the profile image from the bitmap
     * @param bitmap the bitmap to save profile photo as
     */
    private void setProfileImage(Bitmap bitmap) {
        profileImage.setImageBitmap(bitmap);
        profile.setProfileImage(bitmap);
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_PIC_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isCamera;
                if (data == null) {
                    isCamera = false;
                } else {
                    isCamera = data.hasExtra("data");
                }

                Uri selectedImageUri;
                if (isCamera) {
                    Bitmap image = (Bitmap)data.getExtras().get("data");
                    profile.setProfileImage(image);
                    processImageBitmap(image);
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    chosenPictureURI = selectedImageUri;
                    processImageUri(selectedImageUri);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, "Profile picture selection cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                doImageError();
            }
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (uploadReference != null) {
            outState.putString(UPLOAD_REF, uploadReference.toString());

            if (chosenPictureURI != null)
                outState.putString(IMAGE_URI, chosenPictureURI.toString());
            else
                outState.putParcelable(IMAGE_BITMAP, chosenPictureBitmap);
        }
    }

    /**
     * Checks storage and camera permissions as this activity requires them
     * @return true if permission granted, false if not
     */
    private boolean checkPermissions() {
        for (String permission : PERMISSIONS_REQUIRED) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    /**
     * Requests the permissions
     */
    private void requestPermission() {
        requestPermissions(
                PERMISSIONS_REQUIRED,
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean succeeded = true;
                for (int i : grantResults) {
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        succeeded = false;
                        break;
                    }
                }

                if (succeeded) {
                    onUploadPermissionsGranted();
                    return;
                }
            }

            Toast.makeText(activity, "Permissions denied", Toast.LENGTH_SHORT).show();
        }
    }
}