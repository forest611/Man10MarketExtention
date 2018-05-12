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
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class Man10MarketExtention extends JavaPlugin implements Listener {

    MySQLAPI mysql = null;
    List<Integer> inta = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("mib").setExecutor(this);
        getCommand("man10itembank").setExecutor(this);
        this.saveDefaultConfig();
        mysql = new MySQLAPI(this, "Man10Sotrage");
        Bukkit.getPluginManager().registerEvents(this, this);
        inta.add(10);
        inta.add(11);
        inta.add(12);
        inta.add(13);
        inta.add(14);
        inta.add(15);
        inta.add(16);
    }

    public Inventory createFistMenu(){
        Inventory inv = Bukkit.createInventory(null, 36,"§2§l動作を選択してください");
        for(int i = 0;i < 36;i++){
            inv.setItem(i, new SItemStack(Material.STAINED_GLASS_PANE).setDamage(3).setDisplayname("").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 10, new SItemStack(Material.CHEST).setDisplayname("§6§lアイテムを保管する").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 19, new SItemStack(Material.CHEST).setDisplayname("§6§lアイテムを保管する").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 14, new SItemStack(Material.DISPENSER).setDisplayname("§7§lアイテムを取り出す").build());
        }
        for(int i = 0;i < 3;i++){
            inv.setItem(i + 23, new SItemStack(Material.DISPENSER).setDisplayname("§7§lアイテムを取り出す").build());
        }
        return inv;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public HashMap<UUID, String> invMap = new HashMap<>();

    public Inventory createStoreInventory(){
        Inventory inv = Bukkit.createInventory(null, 54, "§2§l転送するアイテムを入れてください");
        for(int i = 0;i < 9;i++) {
            inv.setItem(45 + i, new SItemStack(Material.STAINED_GLASS_PANE).setDisplayname(" ").setDamage(3).build());
        }
        ItemStack item = new SItemStack(Material.STAINED_GLASS_PANE).setDisplayname("§c§l決定").setDamage(14).build();
        inv.setItem(48, item);
        inv.setItem(49, item);
        inv.setItem(50, item);

        return inv;
    }

    public Inventory createTakeInventory(UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 54, "§2§l転送するアイテムを選択してください");
        for(int i = 0;i < 9;i++) {
            inv.setItem(45 + i, new SItemStack(Material.STAINED_GLASS_PANE).setDisplayname(" ").setDamage(3).build());
        }
        ItemStack next = new SItemStack(Material.STAINED_GLASS_PANE).setDisplayname("§c§l次").setDamage(14).build();
        ItemStack back = new SItemStack(Material.STAINED_GLASS_PANE).setDisplayname("§c§l前").setDamage(14).build();
        inv.setItem(45, back);
        inv.setItem(46, back);
        inv.setItem(53, next);
        inv.setItem(52, next);
        List<ItemStack> a =   getMarketItem();
        HashMap<ItemStack, StorageSpace> sp = getPlayerStorageData(uuid);
        List<ItemStack> items = getPlayerStorage(uuid);
        int size = a.size();
        if (size >= 46){
            size = 45;
        }
        List<Integer> aa = new ArrayList<>();
        for(int i = 0;i < size;i++){
            aa.add(itemMap.get(a.get(i)));
            if (items.contains(a.get(i))){
                StorageSpace data = sp.get(a.get(i));
                if(data.amount == 0){
                    inv.setItem(i, new SItemStack(a.get(i).getType()).setDamage(itemMapRev.get(itemMap.get(a.get(i))).getDurability()).setDisplayname(itemNameMap.get(itemMap.get(a.get(i)))).addLore("§a所有量:§c"  +0).addLore("§a単価:§c" +0).addLore("§a推定価値:§c" +0).build());
                }else {
                    inv.setItem(i, new SItemStack(a.get(i).getType()).setDamage(itemMapRev.get(itemMap.get(a.get(i))).getDurability()).setDisplayname(data.key).addLore("§a所有量:§c" + data.amount).addLore("§a単価:§c" + price.get(a.get(i))).addLore("§a推定価値:§c" + price.get(a.get(i)) * data.amount).setGlowingEffect(true).build());
                }
            }else{
                inv.setItem(i, new SItemStack(a.get(i).getType()).setDamage(itemMapRev.get(itemMap.get(a.get(i))).getDurability()).setDisplayname(itemNameMap.get(itemMap.get(a.get(i)))).addLore("§a所有量:§c"  +0).addLore("§a単価:§c" +0).addLore("§a推定価値:§c" +0).build());
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            if(args[0].equalsIgnoreCase("store")){
                Player p = (Player) sender;
                if(!p.hasPermission("man10.storage.store")){
                    p.sendMessage("§4あなたには権限がありません");
                    return false;
                }
                invMap.put(p.getUniqueId(), "storeMenu");
                p.openInventory(createStoreInventory());
            }
            if(args[0].equalsIgnoreCase("take")){
                Player p = (Player) sender;
                if(!p.hasPermission("man10.storage.take")){
                    p.sendMessage("§4あなたには権限がありません");
                    return false;
                }
                invMap.put(p.getUniqueId(), "takeMenu");
                p.openInventory(createTakeInventory(p.getUniqueId()));
            }
        }else{
            Player p = (Player) sender;
            if(!p.hasPermission("man10.storage.main")){
                p.sendMessage("§4あなたには権限がありません");
                return false;
            }
            invMap.put(p.getUniqueId(), "mainMenu");
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

    public Inventory createControllMenu(String name, UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 27, "§a" + name + "に対する操作を選んでください");
        for(int i = 0;i < 27;i++){
            inv.setItem(i, new SItemStack(Material.STAINED_GLASS_PANE).setDamage(3).build());
        }
        inv.setItem(11, new SItemStack(Material.REDSTONE_BLOCK).setDisplayname("§c§l買い注文を入れる").build());
        inv.setItem(15, new SItemStack(Material.EMERALD_BLOCK).setDisplayname("§a§l売り注文を入れる").build());
        inv.setItem(13, new SItemStack(Material.CHEST).setDisplayname("§6§l倉庫から引き出す").build());
        return inv;
    }

    public Inventory createPullMenu(UUID uuid){
        Inventory inv = Bukkit.createInventory(null, 27, "§a引き出す数を選んでください");
        for(int i = 0;i < 27;i++){
            inv.setItem(i, new SItemStack(Material.STAINED_GLASS_PANE).setDamage(3).build());
        }
        for(int i = 0;i < 7;i++){
            inv.setItem(10 + i, new SItemStack(itemMapRev.get(inventoryData.get(uuid)).getType()).setAmount((int) Math.pow(2, i)).build());
        }
        return inv;
    }


    @EventHandler
    public void click(InventoryClickEvent e){
        if(!invMap.containsKey(e.getWhoClicked().getUniqueId())){
            return;
        }
        if(invMap.get(e.getWhoClicked().getUniqueId()).equalsIgnoreCase("takeMenu")){
            if(e.getAction() != InventoryAction.PICKUP_ALL){
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            if(e.getInventory().getItem(e.getSlot()).getType() == null){
                return;
            }
            inventoryData.put(e.getWhoClicked().getUniqueId(), inventoryInt.get(e.getSlot()));
            if(!getPlayerStorage(e.getWhoClicked().getUniqueId()).contains(itemMapRev.get(inventoryData.get(e.getWhoClicked().getUniqueId())))){
                e.getWhoClicked().sendMessage("§c§l資材を所有していません");
                return;
            }
            e.getWhoClicked().openInventory(createPullMenu(e.getWhoClicked().getUniqueId()));
            invMap.put(e.getWhoClicked().getUniqueId(), "pullMenu");
        }
        if(invMap.get(e.getWhoClicked().getUniqueId()).equalsIgnoreCase("mainMenu")){
            e.setCancelled(true);
            int s = e.getSlot();
            Player p = (Player) e.getWhoClicked();
            if(s == 10 || s == 11 || s == 12 || s == 19 || s == 20 || s == 21){
                p.closeInventory();
                p.openInventory(createStoreInventory());
                invMap.put(p.getUniqueId(), "storeMenu");
            }
            if(s == 14 || s == 15 || s == 16 || s == 23 || s == 24 || s == 25){
                p.closeInventory();
                p.openInventory(createTakeInventory(p.getUniqueId()));
                invMap.put(p.getUniqueId(), "takeMenu");
            }

        }
        if(invMap.get(e.getWhoClicked().getUniqueId()).equalsIgnoreCase("pullMenu")){

            int s = e.getSlot();
            if(!inta.contains(s)){
                e.setCancelled(true);
                return;
            }
            if(e.getWhoClicked().getInventory().firstEmpty() == -1){
                e.getWhoClicked().sendMessage("§cインベントリがいっぱいです");
                e.setCancelled(true);
                return;
            }
            HashMap<ItemStack, StorageSpace> a = getPlayerStorageData(e.getWhoClicked().getUniqueId());
            StorageSpace b = a.get(itemMapRev.get(inventoryData.get(e.getWhoClicked().getUniqueId())));
            if(b.amount < e.getInventory().getItem(e.getSlot()).getAmount()){
                e.getWhoClicked().sendMessage("§c資材が不足してます");
                e.setCancelled(true);
                return;
            }
            ItemStack item = itemMapRev.get(inventoryData.get(e.getWhoClicked().getUniqueId()));
            item.setAmount(e.getInventory().getItem(e.getSlot()).getAmount());
            e.getWhoClicked().getInventory().addItem(item);
            mysql.execute("UPDATE item_storage SET amount = amount - " + e.getInventory().getItem(e.getSlot()).getAmount() + " WHERE uuid = '" + e.getWhoClicked().getUniqueId() + "' and item_id = '" + inventoryData.get(e.getWhoClicked().getUniqueId()) + "'");
            e.setCancelled(true);
        }
        if(invMap.get(e.getWhoClicked().getUniqueId()).equalsIgnoreCase("controlMenu")){
            e.setCancelled(true);
            if(e.getSlot() == 15){
                Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "mm price " + itemNameMap.get(inventoryData.get(e.getWhoClicked().getUniqueId())));
                e.getWhoClicked().closeInventory();
            }
            if(e.getSlot() == 11){
                Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "mm price " + itemNameMap.get(inventoryData.get(e.getWhoClicked().getUniqueId())));
                e.getWhoClicked().closeInventory();
            }
            if(e.getSlot() == 13){
                if(!getPlayerStorage(e.getWhoClicked().getUniqueId()).contains(itemMapRev.get(inventoryData.get(e.getWhoClicked().getUniqueId())))){
                    e.getWhoClicked().sendMessage("§c§l資材を所有していません");
                    return;
                }
                e.getWhoClicked().openInventory(createPullMenu(e.getWhoClicked().getUniqueId()));
                invMap.put(e.getWhoClicked().getUniqueId(), "pullMenu");
            }
        }
        if(invMap.get(e.getWhoClicked().getUniqueId()).equalsIgnoreCase("storeMenu")){
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
                    if(!checkIfUserHasStorage(e.getWhoClicked().getUniqueId(), (Integer) tempMap.keySet().toArray()[i])){
                        mysql.execute("INSERT INTO item_storage (`id`,`uuid`,`player`,`item_id`,`key`,`amount`,`datetime`) VALUES " +
                                "('0','" + e.getWhoClicked().getUniqueId() + "','" + e.getWhoClicked().getName() + "','" + tempMap.keySet().toArray()[i] + "','" + itemNameMap.get(tempMap.keySet().toArray()[i]) + "','" + 0 + "','" + mysql.currentTimeNoBracket() + "');");
                    }
                    mysql.execute("UPDATE item_storage SET amount = amount + " + tempMap.get(tempMap.keySet().toArray()[i] ) + " WHERE uuid ='" + e.getWhoClicked().getUniqueId() + "' and item_id =" + tempMap.keySet().toArray()[i]);
                }
                sendMessageOfStoreToPlayer(tempMap, (Player) e.getWhoClicked());
                if(!hasGarbage){
                    e.getWhoClicked().closeInventory();
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
        if(!invMap.containsKey(e.getPlayer().getUniqueId())){
            return;
        }
        if(invMap.get(e.getPlayer().getUniqueId()).equalsIgnoreCase("storeMenu")){
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
        invMap.remove(e.getPlayer().getUniqueId());
    }
}
