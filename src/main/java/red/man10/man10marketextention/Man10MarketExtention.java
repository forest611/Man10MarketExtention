package red.man10.man10marketextention;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import red.man10.man10mysqlapi.MySQLAPI;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Man10MarketExtention extends JavaPlugin implements Listener {

    MySQLAPI mysql = null;
    final List<Integer> inta = new ArrayList<>();

    final List<String> TITLES = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("mib").setExecutor(this);
        getCommand("man10itembank").setExecutor(this);
        this.saveDefaultConfig();
        mysql = new MySQLAPI(this, "Man10Storage");
        Bukkit.getPluginManager().registerEvents(this, this);
        inta.add(10);
        inta.add(11);
        inta.add(12);
        inta.add(13);
        inta.add(14);
        inta.add(15);
        inta.add(16);

        TITLES.add("§2§l転送するアイテムを入れてください");
        TITLES.add("§2§l転送するアイテムを選択してください");
        TITLES.add("§2§l動作を選択してください");
        TITLES.add("§a引き出す数を選んでください");
        TITLES.add("§a操作を選んでください");

    }

    public Inventory createFistMenu(){
        Inventory inv = Bukkit.createInventory(null, 36,"§2§l動作を選択してください");
        for(int i = 0;i < 36;i++){
            inv.setItem(i, new SItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName("").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 10, new SItemStack(Material.CHEST).setDisplayName("§6§lアイテムを保管する").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 19, new SItemStack(Material.CHEST).setDisplayName("§6§lアイテムを保管する").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 14, new SItemStack(Material.DISPENSER).setDisplayName("§7§lアイテムを取り出す").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 23, new SItemStack(Material.DISPENSER).setDisplayName("§7§lアイテムを取り出す").build());
        }
        return inv;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Inventory createStoreInventory(){
        Inventory inv = Bukkit.createInventory(null, 54, "§2§l転送するアイテムを入れてください");
        for(int i = 0;i < 9;i++) {
            inv.setItem(45 + i, new SItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        }
        ItemStack item = new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName("§c§l決定").build();
        inv.setItem(48, item);
        inv.setItem(49, item);
        inv.setItem(50, item);

        return inv;
    }

    public Inventory createTakeInventory(UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 54, "§2§l転送するアイテムを選択してください");
        for(int i = 0;i < 9;i++) {
            inv.setItem(45 + i, new SItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName(" ").build());
        }
        ItemStack next = new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName("§c§l次").build();
        ItemStack back = new SItemStack(Material.RED_STAINED_GLASS_PANE).setDisplayName("§c§l前").build();

        inv.setItem(45, back);
        inv.setItem(46, back);
        inv.setItem(53, next);
        inv.setItem(52, next);

        List<ItemStack> marketItem = getMarketItem();
        HashMap<ItemStack, StorageSpace> sp = getPlayerStorageData(uuid);
        List<ItemStack> items = getPlayerStorage(uuid);
        int size = marketItem.size();
        if(size >=46){
            size = 45;
        }
        int maxpage = marketItem.size()/45;
        int total = inventoryPage.get(uuid) * 45;
        if(maxpage ==  inventoryPage.get(uuid)){
            size = marketItem.size() - total;
        }
        List<Integer> aa = new ArrayList<>();
        for(int i = 0;i < size;i++){
            ItemStack mItem = marketItem.get(total+i);

            int cmd = 0;

            if (mItem.getItemMeta().hasCustomModelData()){
                cmd = mItem.getItemMeta().getCustomModelData();
            }

            aa.add(itemMap.get(mItem));

            if (items.contains(mItem)){
                StorageSpace data = sp.get(mItem);

                SItemStack item = new SItemStack(marketItem.get(total + i).getType()).setCustomModelData(cmd).setDisplayName(mItem.getItemMeta().getDisplayName())
                        .addLore("§a所有量:§c"  +data.amount).addLore("§a単価:§c" +price.get(mItem)).addLore("§a推定価値:§c" +price.get(mItem)*data.amount);

                if(data.amount != 0){
                    item.setGlowingEffect(true);
                }

                inv.setItem(i,item.build());
            }else{
                inv.setItem(i, new SItemStack(marketItem.get(total + i).getType()).setCustomModelData(cmd).setDisplayName(mItem.getItemMeta().getDisplayName())
                        .addLore("§a所有量:§c"  +0).addLore("§a単価:§c" +0).addLore("§a推定価値:§c" +0).build());
            }
        }
        inventoryInt = aa;
        return inv;
    }

    public static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items[0];
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<ItemStack> getMarketItem(){
        ResultSet rs = mysql.query("SELECT price,id,base64,item_key FROM item_index");
        ArrayList<ItemStack> items = new ArrayList<>();
        try {
            while(rs.next()){
                items.add(itemFromBase64(rs.getString("base64")));
                price.put(itemFromBase64(rs.getString("base64")), rs.getDouble("price"));
                itemMap.put(itemFromBase64(rs.getString("base64")), rs.getInt("id"));
                itemNameMap.put(rs.getInt("id"), rs.getString("item_key"));
                itemMapRev.put(rs.getInt("id"), itemFromBase64(rs.getString("base64")));
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public HashMap<ItemStack, Integer> itemMap = new HashMap<>();
    public HashMap<Integer, ItemStack> itemMapRev = new HashMap<>();
    public HashMap<Integer, String> itemNameMap = new HashMap<>();
    public HashMap<ItemStack, Double> price = new HashMap<>();
    public HashMap<UUID, Integer> inventoryData = new HashMap<>();
    public List<Integer> inventoryInt = new ArrayList<>();
    public HashMap<UUID, Integer> inventoryPage = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            if(args[0].equalsIgnoreCase("store")){
                Player p = (Player) sender;
                if(!p.hasPermission("man10.storage.store")){
                    p.sendMessage("§4あなたには権限がありません");
                    return false;
                }
                p.openInventory(createStoreInventory());
            }
            if(args[0].equalsIgnoreCase("take")){
                Player p = (Player) sender;
                if(!p.hasPermission("man10.storage.take")){
                    p.sendMessage("§4あなたには権限がありません");
                    return false;
                }
                p.openInventory(createTakeInventory(p.getUniqueId()));
            }
        }else{
            Player p = (Player) sender;
            if(!p.hasPermission("man10.storage.main")){
                p.sendMessage("§4あなたには権限がありません");
                return false;
            }
            p.openInventory(createFistMenu());
        }
        return true;
    }

    public boolean checkIfUserHasStorage(UUID uuid, int id){
        ResultSet rs = mysql.query("SELECT count(1) FROM item_storage WHERE uuid = '" + uuid + "' and item_id = " + id);
        boolean a = false;
        try {
            while(rs.next()){
                a = mysql.convertMysqlToBoolean(rs.getInt("count(1)"));
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }

    public HashMap<ItemStack, StorageSpace> getPlayerStorageData(UUID uuid){
        ResultSet rs = mysql.query("SELECT item_id,`key`,amount FROM item_storage WHERE UUID = '" + uuid + "' ORDER BY amount DESC");
        HashMap<ItemStack, StorageSpace> out = new HashMap<>();
        try {
            while (rs.next()){
                out.put(itemMapRev.get(rs.getInt("item_id")),  new StorageSpace(rs.getInt("item_id"), rs.getLong("amount"), rs.getString("key")));
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public List<ItemStack> getPlayerStorage(UUID uuid){
        ResultSet rs = mysql.query("SELECT item_id FROM item_storage WHERE UUID = '" + uuid + "' ORDER BY amount DESC");
        List<ItemStack> st = new ArrayList<>();
        try {
            while (rs.next()){
                st.add(itemMapRev.get(rs.getInt("item_id")));
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return st;
    }

    public Inventory createControlMenu(String name, UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 27, "§a操作を選んでください");
        for(int i = 0;i < 27;i++){
            inv.setItem(i, new SItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).build());
        }
        inv.setItem(11, new SItemStack(Material.REDSTONE_BLOCK).setDisplayName("§c§l買い注文を入れる").build());
        inv.setItem(15, new SItemStack(Material.EMERALD_BLOCK).setDisplayName("§a§l売り注文を入れる").build());
        inv.setItem(13, new SItemStack(Material.CHEST).setDisplayName("§6§l倉庫から引き出す").build());
        return inv;
    }

    public Inventory createPullMenu(UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 27, "§a引き出す数を選んでください");
        for(int i = 0;i < 27;i++){
            inv.setItem(i, new SItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE).build());
        }
        for(int i = 0;i < 7;i++){
            inv.setItem(10 + i, new SItemStack(itemMapRev.get(inventoryData.get(uuid)).getType()).setAmount((int) Math.pow(2, i)).build());
        }
        return inv;
    }


    @EventHandler
    public void click(InventoryClickEvent e){

        Player p = (Player) e.getWhoClicked();

        if (!TITLES.contains(p.getOpenInventory().getTitle()))return;

        if(p.getOpenInventory().getTitle().equals("§2§l転送するアイテムを選択してください")){
            if(e.getAction() != InventoryAction.PICKUP_ALL){
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            int s = e.getSlot();
            if(s == 53 || s == 52){
                List<ItemStack> a =   getMarketItem();
                int maxpage = a.size()/45;
                if(maxpage ==  inventoryPage.get(p.getUniqueId())){
                    return;
                }
                inventoryPage.put(p.getUniqueId(), inventoryPage.get(p.getUniqueId()) + 1);
                p.closeInventory();
                p.openInventory(createTakeInventory(p.getUniqueId()));
                return;
            }
            if(s == 46 || s == 45){
                if(0 ==  inventoryPage.get(p.getUniqueId())){
                    return;
                }
                inventoryPage.put(p.getUniqueId(), inventoryPage.get(p.getUniqueId()) - 1);
                p.closeInventory();
                p.openInventory(createTakeInventory(p.getUniqueId()));
                return;
            }
            if (s == 47 || s == 48 || s == 49 || s == 50 || s == 51) return;
            inventoryData.put(p.getUniqueId(), inventoryInt.get(e.getSlot()));
            if(!getPlayerStorage(p.getUniqueId()).contains(itemMapRev.get(inventoryData.get(p.getUniqueId())))){
                p.sendMessage("§c§l資材を所有していません");
                return;
            }
            p.openInventory(createPullMenu(p.getUniqueId()));
            return;
        }
        if(p.getOpenInventory().getTitle().equals("§2§l動作を選択してください")){
            e.setCancelled(true);

            int s = e.getSlot();
            if(s == 10 || s == 11 || s == 12 || s == 19 || s == 20 || s == 21){
                p.closeInventory();
                p.openInventory(createStoreInventory());
            }
            if(s == 14 || s == 15 || s == 16 || s == 23 || s == 24 || s == 25){
                inventoryPage.put(p.getUniqueId(), 0);
                p.closeInventory();
                p.openInventory(createTakeInventory(p.getUniqueId()));
            }

        }
        if(p.getOpenInventory().getTitle().equals("§a引き出す数を選んでください")){

            int s = e.getSlot();
            if(!inta.contains(s)){
                e.setCancelled(true);
                return;
            }
            if(p.getInventory().firstEmpty() == -1){
                p.sendMessage("§cインベントリがいっぱいです");
                e.setCancelled(true);
                return;
            }
            HashMap<ItemStack, StorageSpace> a = getPlayerStorageData(p.getUniqueId());
            StorageSpace b = a.get(itemMapRev.get(inventoryData.get(p.getUniqueId())));
            if(b.amount < e.getInventory().getItem(e.getSlot()).getAmount()){
                p.sendMessage("§c資材が不足してます");
                e.setCancelled(true);
                return;
            }
            ItemStack item = itemMapRev.get(inventoryData.get(p.getUniqueId()));
            item.setAmount(e.getInventory().getItem(e.getSlot()).getAmount());
            p.getInventory().addItem(item);
            mysql.execute("UPDATE item_storage SET amount = amount - " + e.getInventory().getItem(e.getSlot()).getAmount() + " WHERE uuid = '" + p.getUniqueId() + "' and item_id = '" + inventoryData.get(p.getUniqueId()) + "'");
            e.setCancelled(true);
        }
        if(p.getOpenInventory().getTitle().equals("§a操作を選んでください")){
            e.setCancelled(true);
            if(e.getSlot() == 15){
                Bukkit.getServer().dispatchCommand(p, "mm price " + itemNameMap.get(inventoryData.get(p.getUniqueId())));
                p.closeInventory();
            }
            if(e.getSlot() == 11){
                Bukkit.getServer().dispatchCommand(p, "mm price " + itemNameMap.get(inventoryData.get(p.getUniqueId())));
                p.closeInventory();
            }
            if(e.getSlot() == 13){
                if(!getPlayerStorage(p.getUniqueId()).contains(itemMapRev.get(inventoryData.get(p.getUniqueId())))){
                    p.sendMessage("§c§l資材を所有していません");
                    return;
                }
                p.openInventory(createPullMenu(p.getUniqueId()));
            }
        }
        if( p.getOpenInventory().getTitle().equals("§2§l転送するアイテムを入れてください")){
            List<Integer> a = new ArrayList<>();
            for(int i = 45;i < 54;i++){
                a.add(i);
            }
            HashMap<Integer, Long> tempMap = new HashMap<>();
            if (a.contains(e.getSlot())){
                e.setCancelled(true);
                ArrayList<ItemStack> items = getMarketItem();
                for(int i = 0;i < 45;i++){
                    if(e.getInventory().getItem(i) != null){
                        ItemStack checkItem = e.getInventory().getItem(i).clone();
                        checkItem.setAmount(1);
                        if(items.contains(checkItem)){
                            if(!tempMap.containsKey(itemMap.get(checkItem))){
                                tempMap.put(itemMap.get(checkItem), 0L);
                            }
                            tempMap.put(itemMap.get(checkItem), tempMap.get(itemMap.get(checkItem)) + e.getInventory().getItem(i).getAmount());
                            e.getInventory().setItem(i, new ItemStack(Material.AIR));
                        }
                    }
                }
                boolean hasGarbage = false;
                for(int i = 0;i < 45;i++){
                    if(e.getInventory().getItem(i) != null){
                        hasGarbage = true;
                    }
                }
                for(int i = 0;i < tempMap.size();i++){
                    if(!checkIfUserHasStorage(p.getUniqueId(), (Integer) tempMap.keySet().toArray()[i])){
                        mysql.execute("INSERT INTO item_storage (`id`,`uuid`,`player`,`item_id`,`key`,`amount`,`datetime`) VALUES " +
                                "('0','" + p.getUniqueId() + "','" + p.getName() + "','" + tempMap.keySet().toArray()[i] + "','" + itemNameMap.get(tempMap.keySet().toArray()[i]) + "','" + 0 + "','" + mysql.currentTimeNoBracket() + "');");
                    }
                    mysql.execute("UPDATE item_storage SET amount = amount + " + tempMap.get(tempMap.keySet().toArray()[i] ) + " WHERE uuid ='" + p.getUniqueId() + "' and item_id =" + tempMap.keySet().toArray()[i]);
                }
                sendMessageOfStoreToPlayer(tempMap, (Player) p);
                if(!hasGarbage){
                    p.closeInventory();
                }
            }
        }
    }


    public void sendMessageOfStoreToPlayer(HashMap<Integer, Long> map, Player p){
        for(int i = 0;i < map.size();i++){
            String itemName = itemNameMap.get(map.keySet().toArray()[i]);
            Long amount = map.get(map.keySet().toArray()[i]);
            p.sendMessage(itemName + "が" + amount + "個登録されました");
        }
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent e){
        if(e.getPlayer().getOpenInventory().getTitle().equals("§2§l転送するアイテムを入れてください")){
            HashMap<Integer, Long> tempMap = new HashMap<>();
            ArrayList<ItemStack> items = getMarketItem();
            for(int i = 0;i < 45;i++){
                if(e.getInventory().getItem(i) != null){
                    ItemStack checkItem = e.getInventory().getItem(i).clone();
                    checkItem.setAmount(1);
                    if(items.contains(checkItem)){
                        if(!tempMap.containsKey(itemMap.get(checkItem))){
                            tempMap.put(itemMap.get(checkItem), 0L);
                        }
                        tempMap.put(itemMap.get(checkItem), tempMap.get(itemMap.get(checkItem)) + e.getInventory().getItem(i).getAmount());
                        e.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                }
            }
            for(int i = 0;i < tempMap.size();i++){
                if(!checkIfUserHasStorage(e.getPlayer().getUniqueId(), (Integer) tempMap.keySet().toArray()[i])){
                    mysql.execute("INSERT INTO item_storage (`id`,`uuid`,`player`,`item_id`,`key`,`amount`,`datetime`) VALUES " +
                            "('0','" + e.getPlayer().getUniqueId() + "','" + e.getPlayer().getName() + "','" + tempMap.keySet().toArray()[i] + "','" + itemNameMap.get(tempMap.keySet().toArray()[i]) + "','" + 0 + "','" + mysql.currentTimeNoBracket() + "');");
                }
                mysql.execute("UPDATE item_storage SET amount = amount + " + tempMap.get(tempMap.keySet().toArray()[i] ) + " WHERE uuid ='" + e.getPlayer().getUniqueId() + "' and item_id =" + tempMap.keySet().toArray()[i]);
            }
            sendMessageOfStoreToPlayer(tempMap, (Player) e.getPlayer());
        }
    }
}
