package ie.ul.fitbook.ui.home;

public class Model {

    String id, tile, post;

    public Model(){
}
    public Model(String id, String tile, String post){
        this.id = id;
        this.tile = tile;
        this.post = post;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
