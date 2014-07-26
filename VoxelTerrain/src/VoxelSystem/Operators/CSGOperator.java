package VoxelSystem.Operators;

import VoxelSystem.VoxelData.VoxelExtractor;

public interface CSGOperator {
	public VoxelExtractor operate(VoxelExtractor ... arguements);
}
