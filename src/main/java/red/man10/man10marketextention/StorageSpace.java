package red.man10.man10marketextention;

/**
 * Created by sho on 2018/04/30.
 */
public class StorageSpace {
    int id;
    long amount;
    String key;
    public StorageSpace(int itemId, long amount, String key){
        this.id = itemId;
        this.amount = amount;
        this.key = key;
    }
}
