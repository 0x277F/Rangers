package net.coasterman10.rangers;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyChunkGenerator extends ChunkGenerator {
	@SuppressWarnings("deprecation")
	@Override
	public byte[] generate(World world, Random rand, int cx, int cz) {
		byte[] blocks = new byte[32768];
		if (cx == 0 && cz == 0)
			blocks[64] = (byte) Material.BEDROCK.getId();
		return blocks;
	}
}
