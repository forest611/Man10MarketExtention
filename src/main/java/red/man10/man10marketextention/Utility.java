package red.man10.man10marketextention;

public class Utility {

    public static String priceString(double price) {
        return String.format("$%,d", (int)price);
    }

    public static String itemString(long amount) {
        return String.format("$%,d個", amount);
    }

}
