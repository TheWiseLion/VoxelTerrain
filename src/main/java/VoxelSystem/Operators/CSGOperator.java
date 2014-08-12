package voxelsystem.operators;

import voxelsystem.voxeldata.VoxelExtractor;

public interface CSGOperator {
	public VoxelExtractor operate(VoxelExtractor ... arguements);
}
