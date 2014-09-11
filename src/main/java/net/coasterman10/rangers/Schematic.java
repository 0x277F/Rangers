package net.coasterman10.rangers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class Schematic {
    private String filename;
    private BlockData[][][] blocks;

    public Schematic() {
        filename = "empty-schematic.schematic";
        blocks = new BlockData[0][0][0];
    }

    public Schematic(File file) throws IOException, InvalidSchematicException {
        filename = file.getName();
        blocks = loadMCEditSchematicBlocks(file);
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void build(Location origin) {
        World w = origin.getWorld();
        int x0 = origin.getBlockX();
        int y0 = origin.getBlockY();
        int z0 = origin.getBlockZ();
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                for (int k = 0; k < getLength(); k++) {
                    blocks[i][j][k].build(w.getBlockAt(x0 + i, y0 + j, z0 + k));
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
                        blocks[i][j][k].build(w.getBlockAt(x0 + i, y0 + j, z0 + k));
                    }
                }
                pos++;
                if (pos == getLength())
                    cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public int getWidth() {
        return blocks.length;
    }

    public int getHeight() {
        return blocks[0].length;
    }

    public int getLength() {
        return blocks[0][0].length;
    }

    public int getTypeId(Vector v) {
        return getTypeId(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public int getTypeId(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return 0;
        }
        return blocks[x][y][z].id;
    }

    public byte getData(Vector v) {
        return getData(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    public byte getData(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return (byte) 0;
        }
        return blocks[x][y][z].data;
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

    private static BlockData[][][] loadMCEditSchematicBlocks(File file) throws IOException, InvalidSchematicException {
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

        short width = getShortTag(schematic, "Width");
        short length = getShortTag(schematic, "Length");
        short height = getShortTag(schematic, "Height");

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

        BlockData[][][] blocks = new BlockData[width][height][length];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int i = y * width * length + z * width + x;
                    blocks[x][y][z] = new BlockData(ids[i], data[i]);
                }
            }
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
                    blocks[x][y][z] = new SignBlockData(blocks[x][y][z].id, blocks[x][y][z].data, lines);
                }
            }
        }

        return blocks;
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
        public final int id;
        public final byte data;

        public BlockData(int id, byte data) {
            this.id = id;
            this.data = data;
        }

        @SuppressWarnings("deprecation")
        public void build(Block b) {
            b.setTypeId(id);
            b.setData(data);
        }
    }

    private static class SignBlockData extends BlockData {
        private String[] lines;

        public SignBlockData(int id, byte data, String[] lines) {
            super(id, data);
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
