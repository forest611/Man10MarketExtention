package red.man10.man10marketextention;

import com.google.common.collect.ForwardingMultimap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class SItemStack {

    private ItemStack item = null;
    private int amount = 1;
    private String displayName = null;
    private List<String> lore = new ArrayList<>();
    private List<SEnchant> enchantments = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private int customModelData = 0;
    private boolean isGlowing = false;
    private boolean isUnbreakable = false;
    private boolean skullMode = false;

    //skull builder part ↓

    private String url = null;
    private String skullOwner = null;

    /*
    Created By Sho in 2017/08/16 in Osaka
     */

    public SItemStack(Material material){
        item = new ItemStack(material);
    }

    private class SEnchant{
        public SEnchant (Enchantment ench, int leve){
            this.ench = ench;
            this.level = leve;
        }
        Enchantment ench;
        int level;
    }

    public SItemStack setAmount(int amount){
        this.amount = amount;
        return this;
    }

    public SItemStack setDisplayname(String name){
        this.displayName = name;
        return this;
    }

    public SItemStack addLore(String lore){
        this.lore.add(lore);
        return this;
    }

    public SItemStack setItemLore(List<String> lores){
        lore = lores;
        return this;
    }

    public SItemStack addEnchantment(Enchantment enchant, int level){
        SEnchant s = new SEnchant(enchant,level);
        enchantments.add(s);
        return this;
    }

    public SItemStack addFlag(ItemFlag itemFlag){
        this.flags.add(itemFlag);
        return this;
    }

    public SItemStack setFlags(List<ItemFlag> itemFlags){
        this.flags = flags;
        return this;
    }

    public SItemStack setCustomModelData(int cmd){
        this.customModelData = cmd;
        return this;
    }

    public SItemStack setGlowingEffect(boolean enabled){
        isGlowing = enabled;
        return this;
    }

    public SItemStack setUnBreakable(boolean enabled){
        isUnbreakable = enabled;
        return this;
    }

//    public SItemStack setSkullPlayer(String skullOwner){
//        this.skullMode = true;
//        this.item = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
//        this.skullOwner = skullOwner;
//        return this;
//    }
//
//    public SItemStack setSkullUrl(String url){
//        this.skullMode = true;
//        this.item = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
//        this.url = url;
//        return this;
//    }

    private void resetSettings(){
        item = null;
        amount = 0;
        item = null;
        amount = 1;
        displayName = null;
        lore = new ArrayList<>();
        enchantments = new ArrayList<>();
        flags = new ArrayList<>();
        customModelData = 0;
        isGlowing = false;
        isUnbreakable = false;
        skullMode = false;
        url = null;
        skullOwner = null;
    }

    public SItemStack convertItemStackToSItemStack(ItemStack item){
        resetSettings();
        this.item = new ItemStack(item.getType());
        this.amount = item.getAmount();
        this.displayName = item.getItemMeta().getDisplayName();
        this.lore = item.getItemMeta().getLore();
        for(int i = 0; i < item.getItemMeta().getEnchants().size(); i++){
            SEnchant se = new SEnchant((Enchantment) item.getEnchantments().keySet().toArray()[i],item.getEnchantments().get(item.getEnchantments().keySet().toArray()[i]));
            this.enchantments.add(se);
        }
        this.flags = (List<ItemFlag>) item.getItemMeta().getItemFlags();
        this.customModelData = item.getDurability();
        this.isGlowing = item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);
        this.isUnbreakable = item.getItemMeta().isUnbreakable();

        return this;
    }

    public String getBase64(){
        ItemStack item = this.build();
        return Base64.getEncoder().encode(item.toString().getBytes()).toString();
    }

    public ItemStack build(){
        ItemStack item = this.item;
        item.setAmount(this.amount);
        item.setDurability((short) this.customModelData);
        if(!skullMode){
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(this.displayName);
            if(!this.lore.isEmpty()){
                itemMeta.setLore(this.lore);
            }
            itemMeta.setUnbreakable(this.isUnbreakable);
            for(int i = 0; i < this.enchantments.size();i ++){
                itemMeta.addEnchant(this.enchantments.get(i).ench,this.enchantments.get(i).level,true);
            }
            for (int i = 0; i < this.flags.size();i++){
                itemMeta.addItemFlags(this.flags.get(i));
            }
            if(this.isGlowing){
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.addEnchant(Enchantment.LURE, 1,true);
            }
            item.setItemMeta(itemMeta);
        }else{
            item.setDurability((short)3);
            SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
            if(skullOwner != null){
                itemMeta.setOwner(this.skullOwner);
            }
            if(url != null){
                loadProfile(itemMeta, url);
            }
            if(!this.lore.isEmpty()){
                itemMeta.setLore(this.lore);
            }
            itemMeta.setUnbreakable(this.isUnbreakable);
            for(int i = 0; i < this.enchantments.size();i ++){
                itemMeta.addEnchant(this.enchantments.get(i).ench,this.enchantments.get(i).level,true);
            }
            for (int i = 0; i < this.flags.size();i++){
                itemMeta.addItemFlags(this.flags.get(i));
            }
            if(this.isGlowing){
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.addEnchant(Enchantment.LURE, 1,true);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }



    private void loadProfile(ItemMeta meta, String url) {

        Class<?> profileClass = Reflection.getClass("com.mojang.authlib.GameProfile");

        Constructor<?> profileConstructor = Reflection.getDeclaredConstructor(profileClass, UUID.class, String.class);

        Object profile = Reflection.newInstance(profileConstructor, UUID.randomUUID(), null);

        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());

        Method getPropertiesMethod = Reflection.getDeclaredMethod(profileClass, "getProperties");

        Object propertyMap = Reflection.invoke(getPropertiesMethod, profile);

        Class<?> propertyClass = Reflection.getClass("com.mojang.authlib.properties.Property");

        Reflection.invoke(
                Reflection.getDeclaredMethod(
                        ForwardingMultimap.class, "put", Object.class, Object.class
                ),
                propertyMap,
                "textures",
                Reflection.newInstance(Reflection.getDeclaredConstructor(propertyClass, String.class, String.class), "textures", new String(encodedData))
        );

        Reflection.setField("profile", meta, profile);
    }

    private static final class Reflection {

        private static Class<?> getClass(String forName) {
            try {
                return Class.forName(forName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... params) {
            try {
                return clazz.getDeclaredConstructor(params);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static <T> T newInstance(Constructor<T> constructor, Object... params) {
            try {
                return constructor.newInstance(params);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                return null;
            }
        }

        private static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... params) {
            try {
                return clazz.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private static Object invoke(Method method, Object object, Object... params) {
            method.setAccessible(true);
            try {
                return method.invoke(object, params);
            } catch (InvocationTargetException | IllegalAccessException e) {
                return null;
            }
        }

        private static void setField(String name, Object instance, Object value) {
            Field field = getDeclaredField(instance.getClass(), name);
            field.setAccessible(true);
            try {
                field.set(instance, value);
            } catch (IllegalAccessException ignored) {}
        }

        private static Field getDeclaredField(Class<?> clazz, String name) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                return null;
            }
        }

    }


}