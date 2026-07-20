import java.io.File;
public class TestUri {
    public static void main(String[] args) {
        File root = new File("recipes");
        File file = new File("recipes/handcraft/vanilla_book.json");
        System.out.println(root.toURI().relativize(file.toURI()).getPath());
        
        File fileWin = new File("recipes\handcraft\vanilla_book.json");
        System.out.println(root.toURI().relativize(fileWin.toURI()).getPath());
    }
}
