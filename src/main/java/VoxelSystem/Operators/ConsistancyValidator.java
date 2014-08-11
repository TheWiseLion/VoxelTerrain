package VoxelSystem.Operators;


import java.util.Map;

import VoxelSystem.VoxelData.Chunk;
import VoxelSystem.VoxelData.HermiteEdge;

import com.jme3.math.Vector3f;

/***
 * This helper class that is useful when your making 
 * new and strange operations. Due to the nature of 
 * operations being tricky to debug this class will validate and
 * identify exactly where (and why) a written operation is wrong.
 * 
 * Specifically this class will identify when an operation gives an 
 * inconsistent result (I.E. When the extracted data tells of an edge but none is given.)
 *
 */
public class ConsistancyValidator {
//	public static boolean checkConsistancy(Vector3f p, int dx, int dy, int dz, float res){
//		Map<Chunk.Pair, HermiteEdge> mapping;
//	}
}
