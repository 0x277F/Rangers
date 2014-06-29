package net.coasterman10.rangers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class Schematic {
    private BlockData[][][] blocks;

    public Schematic() {
        blocks = new BlockData[0][0][0];
    }
    
    public Schematic(File file) throws IOException, InvalidSchematicException {
        blocks = loadMCEditSchematicBlocks(file);
    }
    
    @SuppressWarnings("deprecation")
    public void build(Location origin) {
        World w = origin.getWorld();
        int x0 = origin.getBlockX();
        int y0 = origin.getBlockY();
        int z0 = origin.getBlockZ();
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                for (int k = 0; k < getLength(); k++) {
                    Block b = w.getBlockAt(x0 + i, y0 + j, z0 + k);
                    b.setTypeId(blocks[i][j][k].id);
                    b.setData(blocks[i][j][k].data);
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
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
                        Block b = w.getBlockAt(x0 + i, y0 + j, z0 + k);
                        b.setTypeId(blocks[i][j][k].id);
                        b.setData(blocks[i][j][k].data);
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
    }
}
