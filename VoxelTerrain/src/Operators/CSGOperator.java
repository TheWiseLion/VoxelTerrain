package Operators;
import DensityVolumes.DensityVolume;

/***
 * Something that does an operation on a set 
 * @author wiselion
 *
 */
public interface CSGOperator {
	
	/**
	 * Applies the operator in the following order:
	 * dv1 operator dv2.
	 * 
	 * In general the following rules are to be strictly followed:
	 * - discrete + non-discrete = discrete 
	 * - discrete + discrete = discrete (volume of first)
	 * - non-discrete + non-discrete = non-discrete 
	 * 
	 */
	public DensityVolume opperate(DensityVolume dv1,DensityVolume dv2);
}
