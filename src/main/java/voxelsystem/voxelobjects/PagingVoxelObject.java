package voxelsystem.voxelobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import voxelsystem.VoxelSystemTables.AXIS;
import voxelsystem.meshbuilding.MeshOutput;
import voxelsystem.operators.CSGOperator;
import voxelsystem.voxeldata.ExtractorBase;
import voxelsystem.voxeldata.HermiteEdge;
import voxelsystem.voxeldata.VoxelExtractor;
import voxelsystem.voxelmaterials.VoxelType;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;

public class PagingVoxelObject extends ExtractorBase {
	private class QueuedExtraction {
		QueuedExtraction(final VoxelExtractor ve, final VoxelNode w, final BoundingBox bb) {
			this.vn = w;
			this.ve = ve;
			this.bb = bb;
		}

		BoundingBox		bb;
		VoxelExtractor	ve;
		VoxelNode		vn;

		public void preformExtraction() {
			final BoundingBox box = this.bb;
			final int vMin[] = new int[3];
			final int vMax[] = new int[3];
			if (box != null) {
				final Vector3f min = box.getMin(new Vector3f());
				final Vector3f max = box.getMax(new Vector3f());
				vMin[0] = (int) (min.x / PagingVoxelObject.this.voxelSize) - 1;
				vMin[1] = (int) (min.y / PagingVoxelObject.this.voxelSize) - 1;
				vMin[2] = (int) (min.z / PagingVoxelObject.this.voxelSize) - 1;

				vMax[0] = (int) (max.x / PagingVoxelObject.this.voxelSize) + 1;
				vMax[1] = (int) (max.y / PagingVoxelObject.this.voxelSize) + 1;
				vMax[2] = (int) (max.z / PagingVoxelObject.this.voxelSize) + 1;
			} else {
				throw new RuntimeException("Herpy derpy");
			}
			final VoxelNode other = this.vn.deepCopy();
			other.extractVolume(this.ve, vMin, vMax);
			this.vn.copy(other);
			// vn.extractVolume(ve, vMin, vMax);
		}
	}

	/**
	 * Either a voxel extractor or a operation (forms CSG tree)
	 * 
	 * @author 0xFFFF
	 *
	 */
	private static class WorldLayer {
		CSGOperator			operator;
		VoxelExtractor[]	args;

		WorldLayer(final CSGOperator o, final VoxelExtractor[] args) {
			this.operator = o;
			this.args = args;
		}

	}

	int								bitOffSet	= 10;
	int								CHUNK_MASK	= 0x3FF;				// 10 bits -> ~1000 chunks
	int								CHUNK_OFF	= this.CHUNK_MASK / 2;
	int								CHUNK_DIM	= 16;
	private float					voxelSize;
	private boolean					debug;
	Material						debugMats[];

	/***
	 * This stores decompressed chunks. All of the chunk data exists.
	 */
	// Map<Integer, VoxelNode> ChunkCache;

	/***
	 * packed chunks
	 */
	public Map<Integer, VoxelNode>	chunks;

	public LinkedHashSet<VoxelNode>	renderReadyNodes;

	// LinkedList<WorldLayer> worldLayers;//To be applied in order c.extract()c.extract()c.extract()....

	// HashMap<Integer,Integer> chunkQueueStatus;//Maps chunk ID to # of extraction left to perform on it...
	// LinkedList<QueuedExtraction> extractionQueue;

	Vector3f						loadDistance;
	Map<Integer, VoxelType>			materials;

	// TODO: keep a list and recompute if overlap

	public PagingVoxelObject(final float voxelSize, final Map<Integer, VoxelType> iToM, final Vector3f loadDistance) {
		this.voxelSize = voxelSize;
		this.materials = iToM;
		this.loadDistance = loadDistance;

		this.renderReadyNodes = new LinkedHashSet<VoxelNode>();
		// worldLayers = new LinkedList<WorldLayer>();
		//
		// extractionQueue = new LinkedList<QueuedExtraction>();
		// chunkQueueStatus = new HashMap<Integer,Integer>();

		this.chunks = new HashMap<Integer, VoxelNode>();
	}

	// ///////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////
	// // CORE FUNCTIONS ////
	// ///////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////
	public void update(final Application app, final Node root, final int maxWork) {

		if (this.renderReadyNodes.size() > 0) {
			final float error = (float) (Math.sqrt(3) * this.voxelSize / 1000f);
			final Vector3f cP[] = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
			final BoundingBox bb = new BoundingBox();
			bb.setXExtent(this.voxelSize);
			bb.setYExtent(this.voxelSize);
			bb.setZExtent(this.voxelSize);

			final List<Vector3f> normals = new ArrayList<Vector3f>();
			final List<Vector3f> edges = new ArrayList<Vector3f>();
			final int mats[] = new int[8];

			for (final VoxelNode vn : this.renderReadyNodes) {
				vn.genIsopoints(this, cP, edges, normals, mats, bb, error);
			}

			for (final VoxelNode vn : this.renderReadyNodes) {

				if (vn.generated != null) {
					for (final Geometry g : vn.generated) {
						app.enqueue(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								root.detachChild(g);
								return null;
							}
						});
					}
				}

				final Map<Integer, MeshOutput> meshes = vn.genMesh(this, mats);
				vn.generated = new ArrayList<Geometry>();
				for (final Integer i : meshes.keySet()) {
					final MeshOutput mo = meshes.get(i);
					final Mesh[] me = mo.compile(this.debug);
					me[0].updateBound();
					me[0].updateCounts();
					if (me[0].getTriangleCount() > 0) {
						final Geometry g = new Geometry();
						g.setMesh(me[0]);
						g.setMaterial(this.materials.get(i).material);
						g.setShadowMode(ShadowMode.CastAndReceive);
						vn.generated.add(g);
						app.enqueue(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								root.attachChild(g);
								return null;
							}
						});
					}
				}
			}
		}

		this.renderReadyNodes.clear();

	}

	public VoxelNode getChunk(final int x, final int y, final int z) {
		final int key = this.hash(x, y, z);
		VoxelNode vn = this.chunks.get(key);
		if (vn == null) {
			vn = this.createChunkFromKey(key);
			this.chunks.put(key, vn);
		}
		return vn;
	}

	/***
	 * @param operator
	 * @param args
	 *            - any additional arguments for the operator (e.g. other volumes)
	 * @param bb
	 *            - bounding volume used to select which chunks to apply the operator (with the given arguments)
	 */
	public void preformOperation(final CSGOperator operator, final BoundingBox bb, final VoxelExtractor... args) {
		final BoundingBox box = bb;
		if (box == null) {

			return;
		}

		final Vector3f min = box.getMin(new Vector3f());
		final Vector3f max = box.getMax(new Vector3f());
		final int[] vMax = new int[3];
		final int[] vMin = new int[3];

		vMin[0] = (int) (min.x / this.voxelSize) - 1;
		vMin[1] = (int) (min.y / this.voxelSize) - 1;
		vMin[2] = (int) (min.z / this.voxelSize) - 1;

		vMax[0] = (int) (max.x / this.voxelSize) + 1;
		vMax[1] = (int) (max.y / this.voxelSize) + 1;
		vMax[2] = (int) (max.z / this.voxelSize) + 1;

		final List<Integer> chunkKeys = this.chunksFromAABB(box);
		final VoxelExtractor[] args2 = new VoxelExtractor[args.length + 1];
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				args2[i + 1] = args[i];
			}
		}

		for (final int chunk : chunkKeys) {
			VoxelNode c = this.chunks.get(chunk);

			if (c == null) {
				c = this.createChunkFromKey(chunk);
				this.chunks.put(chunk, c);
			}

			VoxelExtractor ve;
			args2[0] = this;
			ve = operator.operate(args2);
			c.extractVolume(ve, vMin, vMax);
			this.renderReadyNodes.add(c);
		}

	}

	public void enableDebugging(final Material normalColor, final Material tangentColor, final Material bitangent) {
		this.debug = true;
		this.debugMats = new Material[3];
		this.debugMats[0] = normalColor;
		this.debugMats[1] = normalColor;
		this.debugMats[2] = normalColor;
	}

	public void disableDebugging() {
		this.debug = false;
		this.debugMats = null;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return null;
	}

	public float getVoxelSize() {
		return this.voxelSize;
	}

	public int getType(final float x, final float y, final float z) {
		return this.getType(this.round(x / this.voxelSize), this.round(y / this.voxelSize), this.round(z / this.voxelSize));
	}

	// ///////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////
	// // HELPER FUNCTIONS ////
	// ///////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////
	int getType(final int wx, final int wy, final int wz) {
		// System.out.println("AIEOUU asking about:" +(wx*voxelSize) +","+(wy*voxelSize)+","+(wz*voxelSize));
		final int chunkX = (int) Math.floor((double) wx / this.CHUNK_DIM);
		final int chunkY = (int) Math.floor((double) wy / this.CHUNK_DIM);
		final int chunkZ = (int) Math.floor((double) wz / this.CHUNK_DIM);
		final VoxelNode vn = this.getChunk(chunkX, chunkY, chunkZ);
		if (vn == null) {
			return -1;
		} else {
			// ask in chunk local space:
			final int cx = wx - chunkX * this.CHUNK_DIM;
			final int cy = wy - chunkY * this.CHUNK_DIM;
			final int cz = wz - chunkZ * this.CHUNK_DIM;

			return vn.getType(cx, cy, cz);
		}
	}

	Float getIntersection(final int wx, final int wy, final int wz, final AXIS a) {

		final int chunkX = (int) Math.floor((double) wx / this.CHUNK_DIM);
		final int chunkY = (int) Math.floor((double) wy / this.CHUNK_DIM);
		final int chunkZ = (int) Math.floor((double) wz / this.CHUNK_DIM);
		final VoxelNode vn = this.getChunk(chunkX, chunkY, chunkZ);
		if (vn == null) {
			return null;
		} else {
			// ask in chunk local space:
			final int cx = wx - chunkX * this.CHUNK_DIM;
			final int cy = wy - chunkY * this.CHUNK_DIM;
			final int cz = wz - chunkZ * this.CHUNK_DIM;

			return vn.getIntersection(cx, cy, cz, a);
		}
	}

	Vector3f getNormal(final int wx, final int wy, final int wz, final AXIS a) {
		final int chunkX = (int) Math.floor((double) wx / this.CHUNK_DIM);
		final int chunkY = (int) Math.floor((double) wy / this.CHUNK_DIM);
		final int chunkZ = (int) Math.floor((double) wz / this.CHUNK_DIM);
		final VoxelNode vn = this.getChunk(chunkX, chunkY, chunkZ);
		if (vn == null) {
			return null;
		} else {
			// ask in chunk local space:
			final int cx = wx - chunkX * this.CHUNK_DIM;
			final int cy = wy - chunkY * this.CHUNK_DIM;
			final int cz = wz - chunkZ * this.CHUNK_DIM;

			return vn.getNormal(cx, cy, cz, a);
		}
	}

	Vector3f getIsopoint(final int wx, final int wy, final int wz) {
		final int chunkX = (int) Math.floor((double) wx / this.CHUNK_DIM);
		final int chunkY = (int) Math.floor((double) wy / this.CHUNK_DIM);
		final int chunkZ = (int) Math.floor((double) wz / this.CHUNK_DIM);
		final VoxelNode vn = this.getChunk(chunkX, chunkY, chunkZ);
		if (vn == null) {
			return null;
		} else {
			// ask in chunk local space:
			final int cx = wx - chunkX * this.CHUNK_DIM;
			final int cy = wy - chunkY * this.CHUNK_DIM;
			final int cz = wz - chunkZ * this.CHUNK_DIM;
			return vn.isoPoints.get(vn.index(cx, cy, cz));
		}
	}

	/**
	 * Retrieves a list of keys to the chunks associated with an AABB.
	 * 
	 * @param bb
	 * @return
	 */
	private List<Integer> chunksFromAABB(final BoundingBox bb) {
		final Vector3f min = bb.getMin(new Vector3f());
		final Vector3f max = bb.getMax(new Vector3f());

		// First clamp to voxel space.
		int minX = (int) (min.x / this.voxelSize) - 1;
		int minY = (int) (min.y / this.voxelSize) - 1;
		int minZ = (int) (min.z / this.voxelSize) - 1;
		int maxX = (int) (max.x / this.voxelSize) + 1;
		int maxY = (int) (max.y / this.voxelSize) + 1;
		int maxZ = (int) (max.z / this.voxelSize) + 1;

		// min.divideLocal(voxelSize);
		// max.divideLocal(voxelSize);

		// Then clamp to chunk space (rounding up)
		minX = this.roundChunkF(minX);
		minY = this.roundChunkF(minY);
		minZ = this.roundChunkF(minZ);

		maxX = this.roundChunkU(maxX);
		maxY = this.roundChunkU(maxY);
		maxZ = this.roundChunkU(maxZ);

		// Then iterate over all the chunks...]
		final ArrayList<Integer> chunks = new ArrayList<Integer>();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					chunks.add(this.hash(x, y, z));
				}
			}
		}
		return chunks;

	}

	private int roundChunkF(final int c) {
		final float x = (float) c / this.CHUNK_DIM;
		return (int) Math.floor(x);
	}

	private int roundChunkU(final int c) {
		final float x = (float) c / this.CHUNK_DIM;
		return (int) Math.ceil(x);
	}

	private int hash(final int x, final int y, final int z) {
		return x + this.CHUNK_OFF & this.CHUNK_MASK | y + this.CHUNK_OFF << this.bitOffSet | z + this.CHUNK_OFF << this.bitOffSet * 2;
	}

	/***
	 * Creates and queue's new chunk (aka queues anything in world base)
	 * 
	 * @param key
	 * @return
	 */
	private VoxelNode createChunkFromKey(final int key) {
		int oX = key & this.CHUNK_MASK;
		int oY = key >> this.bitOffSet & this.CHUNK_MASK;
		int oZ = key >> this.bitOffSet * 2 & this.CHUNK_MASK;
		oX -= this.CHUNK_OFF;
		oY -= this.CHUNK_OFF;
		oZ -= this.CHUNK_OFF;

		// Now put it in units of voxels:
		oX *= this.CHUNK_DIM;
		oY *= this.CHUNK_DIM;
		oZ *= this.CHUNK_DIM;

		return new VoxelNode(oX, oY, oZ, this.CHUNK_DIM, this.CHUNK_DIM, this.CHUNK_DIM, this.voxelSize, key);
	}

	private int round(final float x) {
		int r;
		final float f = Math.abs(x);
		if (f - (int) f >= .5) {
			r = (int) f + 1;
		} else {
			r = (int) f;
		}

		if (x < 0) { // add back the sign
			r = -r;
		}

		return r;
	}

	@Override
	public HermiteEdge getEdge(final Vector3f p1, final Vector3f p2) {
		final int x1 = this.round(p1.x / this.voxelSize);
		final int y1 = this.round(p1.y / this.voxelSize);
		final int z1 = this.round(p1.z / this.voxelSize);

		final int x2 = this.round(p2.x / this.voxelSize);
		final int y2 = this.round(p2.y / this.voxelSize);
		final int z2 = this.round(p2.z / this.voxelSize);

		final HermiteEdge he = new HermiteEdge();
		he.t1 = this.getType(x1, y1, z1);
		he.t2 = this.getType(x2, y2, z2);

		if (he.t1 != he.t2) {
			int vDifference;
			AXIS a;
			if (x1 != x2) {
				vDifference = x1 - x2;
				a = AXIS.X;

			} else if (y1 != y2) {
				vDifference = y1 - y2;
				a = AXIS.Y;

			} else if (z1 != z2) {
				vDifference = z1 - z2;
				a = AXIS.Z;

			} else {
				// Probably arbitrary scaling... which should be legal.....
				throw new RuntimeException("Do not differ by axis");
			}

			// WRONG:
			he.intersection = this.getIntersection(x1, y1, z1, a);
			he.normal = this.getNormal(x1, y1, z1, a);
			// TODO: Scale Search...
		}
		return he;

	}

}
