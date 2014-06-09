package DualContour;

import com.jme3.math.Vector3f;

public class OctreeNode {

	private OctreeNode[] children;;
	private final Vector3f minBound, maxBound;
	private int depth;
	private int vertexIndex;
	
	private OctreeNode(Vector3f minBound, Vector3f maxBound, int depth)
    {
        this.minBound = minBound;
        this.maxBound = maxBound;
        this.depth = depth;
    }

    public OctreeNode(Vector3f minBound, Vector3f maxBound)
    {
        this(minBound, maxBound, 0);
    }

    /**
     * Creates the eight children of this node.
     */
    public void subdivide()
    {
        // First compute the center of the current cell.
        Vector3f center = minBound.add(maxBound).divideLocal(2);

        //The child 0bZYX is located at the X, Y ,Z quadrant.
        children[0] = new OctreeNode(minBound, center, depth + 1);
        children[1] = new OctreeNode(new Vector3f(center.x, minBound.y, minBound.z), new Vector3f(maxBound.x, center.y, center.z), depth + 1);
        children[2] = new OctreeNode(new Vector3f(minBound.x, center.y, minBound.z), new Vector3f(center.x, maxBound.y, center.z), depth + 1);
        children[3] = new OctreeNode(new Vector3f(center.x, center.y, minBound.z), new Vector3f(maxBound.x, maxBound.y, center.z), depth + 1);
        children[4] = new OctreeNode(new Vector3f(minBound.x, minBound.y, center.z), new Vector3f(center.x, center.y, maxBound.z), depth + 1);
        children[5] = new OctreeNode(new Vector3f(center.x, minBound.y, center.z), new Vector3f(maxBound.x, center.y, maxBound.z), depth + 1);
        children[6] = new OctreeNode(new Vector3f(minBound.x, center.y, center.z), new Vector3f(center.x, maxBound.y, maxBound.z), depth + 1);
        children[7] = new OctreeNode(center, maxBound, depth + 1);

        // Free the associated vertex, if there is one.
        vertexIndex = -1;
    }

    /**
     * Returns true iff no child exists.
     */
    public boolean isLeaf()
    {
        // if(vertexIndex)
        for (OctreeNode child : children)
        {
            if (child != null && child != this)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the depth
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * @return the children
     */
    public OctreeNode[] getChildren()
    {
        return children;
    }

    /**
     * @return the minBound
     */
    public Vector3f getMinBound()
    {
        return minBound;
    }

    /**
     * @return the maxBound
     */
    public Vector3f getMaxBound()
    {
        return maxBound;
    }

    /**
     * @return the vertex
     */
    public int getVertexIndex()
    {
        return vertexIndex;
    }

    /**
     * @param vertex the vertex to set
     */
    public void setVertex(int vertexIndex)
    {
        this.vertexIndex = vertexIndex;
        // On setting a vertex, make that node a leaf, and make its children 
        // point to itself.
        for (int i = 0; i < 8; i++)
        {
            children[i] = this;
        }
    }

    /**
     * Returns true iff the the vertex is inside the cubes bounds.
     */
    public boolean contains(Vector3f vertex)
    {
        return vertex.x > minBound.x && vertex.y > minBound.y && vertex.z > minBound.z
                && vertex.x < maxBound.x && vertex.y < maxBound.y && vertex.z < maxBound.z;
    }

    /**
     * Returns the location of the given corner.
     */
    public Vector3f getCorner(int corner)
    {
        if (corner == 0)
        {
            return minBound;
        }
        if (corner == 7)
        {
            return maxBound;
        }

        Vector3f result = minBound.clone();
        if ((corner & 1) != 0)
        {
            result.x = maxBound.x;
        }
        if ((corner & 2) != 0)
        {
            result.y = maxBound.y;
        }
        if ((corner & 4) != 0)
        {
            result.z = maxBound.z;
        }

        return result;
    }

    /** Returns the length of the cube's diagonal. */
    public float getCubeDiagonal()
    {
        return maxBound.distance(minBound);
    }
}
