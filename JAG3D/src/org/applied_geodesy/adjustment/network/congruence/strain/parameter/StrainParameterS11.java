package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS11 extends StrainParameter {

	public StrainParameterS11() {
		super(1.0);
	}
	
	public StrainParameterS11(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S11;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}
