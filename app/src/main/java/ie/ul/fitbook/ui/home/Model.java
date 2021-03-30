package ie.ul.fitbook.ui.home;

public class Model {

    String tile, post;

    public Model(){
}
    public Model(String tile, String post){

        this.tile = tile;
        this.post = post;

    }

    //public String getId() {
      //  return id;
    //}

//    public void setId(String id) {
//        this.id = id;
//    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
