package net.coasterman10.rangers.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class Schematic {
    private final File file;
    private int width = 0;
    private int height = 0;
    private int length = 0;
    private BlockData[] blocks = new BlockData[0];

    public Schematic(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }

    public void build(Location origin) {
        World w = origin.getWorld();
        int x0 = origin.getBlockX();
        int y0 = origin.getBlockY();
        int z0 = origin.getBlockZ();
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                for (int k = 0; k < getLength(); k++) {
                    blocks[indexOf(i, j, k)].build(w.getBlockAt(x0 + i, y0 + j, z0 + k));
                }
            }
        }
    }

    public void buildDelayed(final Location origin, Plugin plugin) {
        new BukkitRunnable() {
            final World w = origin.getWorld();
            final int x0 = origin.getBlockX();
            final int y0 = origin.getBlockY();
            final int z0 = origin.getBlockZ();
            int pos;

            @Override
            public void run() {
                if (getWidth() == 0 || getHeight() == 0 || getLength() == 0) {
                    cancel();
                    return;
                }

                for (int i = 0; i < getWidth(); i++) {
                    for (int j = 0; j < getHeight(); j++) {
                        int k = pos;
                        blocks[indexOf(i, j, k)].build(w.getBlockAt(x0 + i, y0 + j, z0 + k));
                    }
                }
                pos++;
                if (pos == getLength())
                    cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getTypeId(Vector v) {
        return getTypeId(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public int getTypeId(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return 0;
        }
        return blocks[indexOf(x, y, z)].data;
    }

    public byte getData(Vector v) {
        return getData(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public byte getData(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return (byte) 0;
        }
        return blocks[indexOf(x, y, z)].data;
    }

    public void loadBlocks(Location min, Location max) {
        width = max.getBlockX() - min.getBlockX() + 1;
        height = max.getBlockY() - min.getBlockY() + 1;
        length = max.getBlockZ() - min.getBlockZ() + 1;
        blocks = new BlockData[width * height * length];
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
                    blocks[indexOf(x - min.getBlockX(), y - min.getBlockY(), z - min.getBlockZ())] = BlockData
                            .fromBlock(min.getWorld().getBlockAt(x, y, z));
    }

    public void save() throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        NBTOutputStream nbtStream = new NBTOutputStream(stream);

        Map<String, Tag> schematic = new LinkedHashMap<>();

        schematic.put("Width", new ShortTag("Width", (short) width));
        schematic.put("Length", new ShortTag("Length", (short) length));
        schematic.put("Height", new ShortTag("Height", (short) height));

        schematic.put("Materials", new StringTag("Materials", "Alpha"));

        byte[] idLow = new byte[width * height * length];
        byte[] idHigh = null;
        byte[] data = new byte[width * height * length];
        
        for (int i = 0; i < idLow.length; i++) {
            @SuppressWarnings("deprecation")
            int id = blocks[i].type.getId();
            idLow[i] = (byte) (id & 0xF);
            if (id > 255) {
                if (idHigh == null)
                    idHigh = new byte[blocks.length];
                if ((i & 1) == 0) {
                    idHigh[i >> 1] = (byte) ((idHigh[i >> 1] & 0xF0) | ((id >> 8) & 0xF));
                } else {
                    idHigh[i >> 1] = (byte) ((idHigh[i >> 1] & 0xF) | ((id >> 8) & 0xF) << 4);
                }
            }
            data[i] = blocks[i].data;
        }

        schematic.put("Blocks", new ByteArrayTag("Blocks", idLow));
        schematic.put("Data", new ByteArrayTag("Data", data));
        if (idHigh != null)
            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", idHigh));

        List<Tag> tileEntities = new ArrayList<>();
        for (int i = 0; i < blocks.length; i++) {
            BlockData block = blocks[i];
            if (block instanceof SignBlockData) {
                Map<String, Tag> values = new LinkedHashMap<>();
                BlockVector coords = coordinatesOf(i);
                values.put("x", new IntTag("x", coords.getBlockX()));
                values.put("y", new IntTag("y", coords.getBlockY()));
                values.put("z", new IntTag("z", coords.getBlockZ()));
                String[] lines = ((SignBlockData) block).lines;
                for (int line = 0; line < 4; line++) {
                    String text = (lines[line] != null ? lines[line] : "");
                    values.put("Text" + (line + 1), new StringTag("Text" + (line + 1), text));
                }
                tileEntities.add(new CompoundTag("Sign", values));
            }
        }

        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));

        CompoundTag schematicTag = new CompoundTag("Schematic", schematic);
        nbtStream.writeTag(schematicTag);
        nbtStream.close();
    }

    private boolean inBounds(int x, int y, int z) {
        if (x < 0 || x >= getWidth()) {
            return false;
        }
        if (y < 0 || y >= getHeight()) {
            return false;
        }
        if (z < 0 || z >= getLength()) {
            return false;
        }
        return true;
    }
    
    public void load() throws IOException, InvalidSchematicException {
        FileInputStream stream = new FileInputStream(file);
        NBTInputStream nbtStream = new NBTInputStream(stream);

        CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
        nbtStream.close();
        if (!schematicTag.getName().equals("Schematic")) {
            throw new InvalidSchematicException("Tag \"Schematic\" does not exist or is not first");
        }

        Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new InvalidSchematicException("Schematic file is missing a \"Blocks\" tag");
        }

        width = getShortTag(schematic, "Width");
        length = getShortTag(schematic, "Length");
        height = getShortTag(schematic, "Height");

        String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new InvalidSchematicException("Schematic file is not an Alpha schematic");
        }

        byte[] idLow = getByteArrayTag(schematic, "Blocks");
        byte[] data = getByteArrayTag(schematic, "Data");
        byte[] idHigh = new byte[0];
        short[] ids = new short[idLow.length];

        if (schematic.containsKey("AddBlocks")) {
            idHigh = getByteArrayTag(schematic, "AddBlocks");
        }

        for (int i = 0; i < idLow.length; i++) {
            if ((i >> 1) >= idHigh.length) {
                ids[i] = (short) (idLow[i] & 0xFF);
            } else {
                if ((i & 1) == 0) {
                    ids[i] = (short) (((idHigh[i >> 1] & 0x0F) << 8) + (idLow[i] & 0xFF));
                } else {
                    ids[i] = (short) (((idHigh[i >> 1] & 0xF0) << 4) + (idLow[i] & 0xFF));
                }
            }
        }

        blocks = new BlockData[width * height * length];
        for (int i = 0; i < width * height * length; i++) {
            @SuppressWarnings("deprecation")
            Material type = Material.getMaterial(ids[i]);
            blocks[i] = new BlockData(type, data[i]);
        }

        List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
        for (Tag tag : tileEntities) {
            if (!(tag instanceof CompoundTag))
                continue;
            CompoundTag t = (CompoundTag) tag;

            String id = "";
            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, Tag> values = new HashMap<String, Tag>();
            for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                if (entry.getKey().equals("x")) {
                    if (entry.getValue() instanceof IntTag) {
                        x = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("y")) {
                    if (entry.getValue() instanceof IntTag) {
                        y = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("z")) {
                    if (entry.getValue() instanceof IntTag) {
                        z = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("id")) {
                    if (entry.getValue() instanceof StringTag) {
                        id = ((StringTag) entry.getValue()).getValue();
                    }
                }
                values.put(entry.getKey(), entry.getValue());
            }

            if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < length) {
                if (id.equals("Sign")) {
                    String[] lines = new String[4];
                    for (int i = 0; i < 4; i++) {
                        Tag text = values.get("Text" + (i + 1));
                        if (text instanceof StringTag)
                            lines[i] = (String) values.get("Text" + (i + 1)).getValue();
                        if (lines[i] == null)
                            lines[i] = "";
                    }
                    int index = indexOf(x, y, z);
                    blocks[index] = new SignBlockData(blocks[index].type, blocks[index].data, lines);
                }
            }
        }
    }

    private int indexOf(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    private BlockVector coordinatesOf(int index) {
        return new BlockVector(index % width, (index / width) % width, index / (width * length));
    }

    private static short getShortTag(Map<String, Tag> tags, String key) throws InvalidSchematicException {
        return getChildTag(tags, key, ShortTag.class).getValue();
    }

    private static byte[] getByteArrayTag(Map<String, Tag> tags, String key) throws InvalidSchematicException {
        return getChildTag(tags, key, ByteArrayTag.class).getValue();
    }

    private static <T extends Tag> T getChildTag(Map<String, Tag> tags, String key, Class<T> expected)
            throws InvalidSchematicException {
        Tag tag = tags.get(key);
        if (tag == null) {
            throw new InvalidSchematicException("Schematic file is missing a \"" + key + "\" tag");
        }
        if (!expected.isInstance(tag)) {
            throw new InvalidSchematicException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    private static class BlockData {
        public final Material type;
        public final byte data;

        public BlockData(Material type, byte data) {
            this.type = type != null ? type : Material.AIR;
            this.data = data;
        }

        @SuppressWarnings("deprecation")
        public void build(Block b) {
            b.setType(type);
            b.setData(data);
        }

        @SuppressWarnings("deprecation")
        public static BlockData fromBlock(Block b) {
            Material type = b.getType();
            byte data = b.getData();
            if (type == Material.SIGN || type == Material.SIGN_POST || type == Material.WALL_SIGN)
                return new SignBlockData(type, data, ((Sign) b.getState()).getLines());
            else
                return new BlockData(type, data);
        }
    }

    private static class SignBlockData extends BlockData {
        private String[] lines;

        public SignBlockData(Material type, byte data, String[] lines) {
            super(type, data);
            this.lines = lines;
        }

        public void build(Block b) {
            super.build(b);
            BlockState state = b.getState();
            if (state instanceof Sign) {
                for (int i = 0; i < 4; i++)
                    ((Sign) state).setLine(i, lines[i]);
                state.update();
            }
        }
    }
}
