package ie.ul.fitbook.profile;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.ul.fitbook.sports.Sport;
import ie.ul.fitbook.utils.Utils;

/**
 * This class represents a user's profile
 */
public class Profile {
    /**
     * The Bitmap representing the profile image for this user
     */
    private Bitmap profileImage;
    /**
     * The name of the user
     */
    private String name;
    /**
     * The city/town the user is in
     */
    private String city;
    /**
     * The state/county the user is in
     */
    private String state;
    /**
     * The country the user is in
     */
    private String country;
    /**
     * This user's favourite sport
     */
    private Sport favouriteSport;
    /**
     * A section to enter biography information
     */
    private String bio;
    /**
     * The information related to the athletic potential of this user
     */
    private AthleticInformation athleticInformation;

    /**
     * The name of the profile document. Pass this into UserDatabase.getChildDocument
     */
    public static final String PROFILE_DOCUMENT = "profile-info/profile";
    /**
     * The key used when mapping the name field in the data being stored in our profile document
     */
    public static final String NAME_KEY = "name";
    /**
     * The key used when mapping the city field in the data being stored in our profile document
     */
    public static final String CITY_KEY = "city";
    /**
     * The key used when mapping the state field in the data being stored in our profile document
     */
    public static final String STATE_KEY = "state";
    /**
     * The key used when mapping the country field in the data being stored in our profile document
     */
    public static final String COUNTRY_KEY = "country";
    /**
     * The key used when mapping the favourite sport field in the data being stored in our profile document
     */
    public static final String FAV_SPORT_KEY = "favourite_sport";
    /**
     * The key used when mapping the biography field in the data being stored in our profile document
     */
    public static final String BIOGRAPHY_KEY = "biography";
    /**
     * The path in storage of the profile image
     */
    public static final String PROFILE_IMAGE_PATH = "images/profile_image.png";

    /**
     * Constructs a Profile instance
     * @param profileImage the image for this profile
     * @param name the name of the user
     * @param city the city the user resides in
     * @param state the state they reside in
     * @param country the country they are a citizen of
     * @param favouriteSport the user's favourite sport
     * @param bio a short biography for the user
     * @param athleticInformation the user's athletic information
     */
    public Profile(@Nullable Bitmap profileImage, @NonNull String name, @NonNull String city, @NonNull String state, @NonNull String country, @NonNull Sport favouriteSport,
                   @Nullable String bio, @NonNull AthleticInformation athleticInformation) {
        this.profileImage = profileImage;
        this.name = name;
        this.city = city;
        this.state = state;
        this.country = country;
        this.favouriteSport = favouriteSport;
        this.bio = bio;
        this.athleticInformation = athleticInformation;
    }

    /**
     * Constructs a default Profile
     */
    public Profile() {
    }

    /**
     * Returns the bitmap of the user's profile image
     * @return profile image of the user as a bitmap
     */
    public Bitmap getProfileImage() {
        return profileImage;
    }

    /**
     * Sets the profile image for this user
     * @param profileImage profile image as a bitmap
     */
    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * Retrieves the name of this user
     * @return name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this user
     * @param name name of user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the city/town the user is residing in
     * @return city/town user lives in
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city/town the user lives in
     * @param city city/town user is living in
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Retrieves the state the user's city is in
     * @return state the user's city is in
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the user's state their city resides in
     * @param state the state the user's city is in
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Retrieves the country the user is in
     * @return country the user is a citizen of
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country the user is in
     * @param country the user is a citizen of
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the user's favourite sport as a capitalised String with the rest of it being lowercase
     * @return the user's favourite sport as a string
     */
    public String getFavouriteSport() {
        return Utils.capitalise(favouriteSport.toString());
    }

    /**
     * Sets the favourite sport of this user
     * @param favouriteSport the favourite sport
     */
    public void setFavouriteSport(Sport favouriteSport) {
        this.favouriteSport = favouriteSport;
    }

    /**
     * Retrieves the biography for this user
     * @return a short bio this user may have
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the biography for this user
     * @param bio the biography to use
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Retrieve the athletic information for this user
     * @return athletic information instance
     */
    public AthleticInformation getAthleticInformation() {
        return  athleticInformation;
    }

    /**
     * Sets the athletic information for this user
     * @param athleticInformation the athletic information to set
     */
    public void setAthleticInformation(AthleticInformation athleticInformation) {
        this.athleticInformation = athleticInformation;
    }

    /**
     * Checks if this is a valid data object for a Profile
     * @param data the data to check
     * @throws IllegalArgumentException if not valid
     */
    private static void checkDataValidity(Map<String, Object> data) {
        List<String> keys = Arrays.asList(NAME_KEY, CITY_KEY, STATE_KEY, COUNTRY_KEY, FAV_SPORT_KEY, BIOGRAPHY_KEY,
                AthleticInformation.DOB_FIELD, AthleticInformation.GENDER_FIELD, AthleticInformation.WEIGHT_FIELD);

        for (String key : data.keySet()) {
            if (!keys.contains(key))
                throw new IllegalArgumentException("The provided data contains an invalid key: " + key + ". One of " + keys + " expected.");
        }
    }

    /**
     * Creates a profile object from the provided data
     * @param data the data to extract the profile from
     * @return the created Profile
     * @throws IllegalArgumentException if the data does not contain the correct keys
     */
    public static Profile from(Map<String, Object> data) {
        checkDataValidity(data);
        String name = (String)data.get(NAME_KEY);
        String city = (String)data.get(CITY_KEY);
        String state = (String)data.get(STATE_KEY);
        String country = (String)data.get(COUNTRY_KEY);
        Sport favourite = Sport.convertToSport((String)data.get(FAV_SPORT_KEY));
        String bio = (String)data.get(BIOGRAPHY_KEY);
        String dateOfBirth = (String)data.get(AthleticInformation.DOB_FIELD);
        Profile.AthleticInformation.Gender gender = AthleticInformation.Gender.convertToGender((String)data.get(AthleticInformation.GENDER_FIELD));
        Double weight = (Double)data.get(AthleticInformation.WEIGHT_FIELD);

        if (weight == null)
            weight = 0.00;

        if (name == null || city == null || state == null)
            throw new IllegalStateException("Mandatory profile fields are missing from the document");

        AthleticInformation athleticInformation = new AthleticInformation(dateOfBirth, gender, weight);

        return new Profile(null, name, city, state, country, favourite, bio, athleticInformation);
    }

    /**
     * Converts this profile to a mapping of String key to it's data
     * @return this object in a mapped form that can be used with a FireStore document
     */
    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();

        data.put(NAME_KEY, name);
        data.put(CITY_KEY, city);
        data.put(STATE_KEY, state);
        data.put(COUNTRY_KEY, country);
        data.put(FAV_SPORT_KEY, getFavouriteSport());
        data.put(BIOGRAPHY_KEY, bio);
        data.put(AthleticInformation.DOB_FIELD, athleticInformation.dateOfBirth);
        data.put(AthleticInformation.GENDER_FIELD, athleticInformation.getGender());
        data.put(AthleticInformation.WEIGHT_FIELD, athleticInformation.weight);

        return data;
    }

    /**
     * This class represents information required for calculating athlete info
     */
    public static class AthleticInformation {
        /**
         * The athlete's date of birth in dd/MM/yyyy
         */
        private String dateOfBirth;
        /**
         * The gender of the user
         */
        private Gender gender;
        /**
         * The weight of the user in kg
         */
        private double weight;

        /**
         * An enum providing the available genders to choose from
         */
        public enum Gender {
            /**
             * Represents a male user
             */
            MALE,
            /**
             * Represents a female user
             */
            FEMALE,
            /**
             * Represents a gender where the user preferred not to state theirs
             */
            OTHER;

            /**
             * Returns the value of the provided string as an enum value
             * @param string the string value to try and parse
             * @return the Gender enum value if found
             * @throws IllegalArgumentException if the provided string does not match any enum value
             */
            public static Gender convertToGender(String string) {
                Gender[] values = Gender.values();

                for (Gender gender: values) {
                    String genderString = gender.toString();

                    if (genderString.equalsIgnoreCase(string))
                        return gender;
                }

                throw new IllegalArgumentException("The String value " + string + " does not match any Gender enum value");
            }
        }

        /**
         * The key used when mapping the fate of birth field in the data being stored in our profile document
         */
        public static final String DOB_FIELD = "date_of_birth";
        /**
         * The key used when mapping the gender field in the data being stored in our profile document
         */
        public static final String GENDER_FIELD = "gender";
        /**
         * The key used when mapping the weight field in the data being stored in our profile document
         */
        public static final String WEIGHT_FIELD = "weight";

        /**
         * The regex used for the date of birth
         */
        public static final String DOB_REGEX = "[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}";

        /**
         * Constructs an athletic information object
         * @param dateOfBirth the date of birth (IllegalArgumentException is thrown if incorrect format)
         * @param gender the gender of this athlete.
         * @param weight the weight of this athlete in KG.
         */
        public AthleticInformation(String dateOfBirth, Gender gender, double weight) {
            setDateOfBirth(dateOfBirth);
            this.gender = gender;
            this.weight = weight;
        }

        /**
         * Retrieves the date of birth of this user
         * @return the date of birth String
         */
        public String getDateOfBirth() {
            return dateOfBirth;
        }

        /**
         * Checks the format of the provided date of birth and throws IllegalArgumentException if not dd/MM/yyyy)
         * @param dateOfBirth the date of birth to set
         */
        public void setDateOfBirth(String dateOfBirth) {
            if (!dateOfBirth.matches(DOB_REGEX))
                throw new IllegalArgumentException("The provided date of birth: " + dateOfBirth + " does not match the regex: " + DOB_REGEX);

            this.dateOfBirth = dateOfBirth;
        }

        /**
         * Returns the gender as a String with first letter capitalised and rest lowercase
         * @return the gender as a string
         */
        public String getGender() {
            return Utils.capitalise(gender.toString());
        }

        /**
         * The gender to set for this user
         * @param gender the gender enum variable
         */
        public void setGender(Gender gender) {
            this.gender = gender;
        }

        /**
         * Retrieves the weight of this user
         * @return the weight in KGs
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Sets the weight of the user in kgs
         * @param weight weight of the user in kgs
         */
        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
