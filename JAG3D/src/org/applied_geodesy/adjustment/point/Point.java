package org.applied_geodesy.adjustment.point;

import org.applied_geodesy.adjustment.ConfidenceRegion;
import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.network.parameter.DeflectionX;
import org.applied_geodesy.adjustment.network.parameter.DeflectionY;
import org.applied_geodesy.adjustment.network.parameter.UnknownParameter;

public abstract class Point extends UnknownParameter {
    // Punktnummer
	private String name = new String();
    // Zeile in Designmatrix; -1 entspricht nicht gesetzt 
	private int rowInJacobiMatrix = -1;	
	// Spalte in der Designmatrix; -1 entspricht nicht gesetzt
	private int colInDesignmatrixOfModelErros = -1;

	// Lotabweichungsparameter fuer den Punkt in X/Y-Richtung
	private DeflectionX deflectionX = new DeflectionX(this);
	private DeflectionY deflectionY = new DeflectionY(this);
    // Grenzwert fuer Null
	private final static double ZERO = Math.sqrt(Constant.EPS);
	protected final double coordinates0[] = new double[3]; //coordinates0[] = new double[this.getDimension()];
	private double coordinates[] = new double[3], //coordinates[] = new double[this.getDimension()],
	               redundancy[]  = new double[this.getDimension()],
	               ep[]          = new double[this.getDimension()],
                   nabla[]       = new double[this.getDimension()],
                   grzw[]        = new double[this.getDimension()],
                   sigma[]       = new double[this.getDimension()],
                   sigma0[]      = new double[this.getDimension()],
                   confidenceAxis[]  = new double[this.getDimension()],
                   confidenceAngles[]= new double[3],
                   principalComponents[]= new double[this.getDimension()],
                   confidenceAxis2D[] = new double[2],
                   confidenceAngle2D = 0,
                   nablaCoVarNable = 0.0,
                   efsp  =  0.0,
    			   Tprio =  0.0,
    			   Tpost =  0.0,
    			   Pprio =  0.0,
    			   Ppost =  0.0,
    			   omega =  0.0;
	private boolean significant = false, considerDeflection = false;

	public Point(String name) throws IllegalArgumentException {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException(this.getClass()+" Punktnummer ungueltig!");
		this.name = name.trim();
		for (int i=0; i<this.getDimension(); i++) 
			this.sigma[i] = -1.0;
	}

	public String getName() {
		return this.name;
	}

	public int getColInDesignmatrixOfModelErros() {
		return this.colInDesignmatrixOfModelErros;
	}

	public void setColInDesignmatrixOfModelErros(int col) {
		this.colInDesignmatrixOfModelErros = col;
	}

	public abstract int getDimension();

	public void setStdX(double std) {
		this.sigma0[0] = (this.sigma0[0] <= 0 && std>0)?std:this.sigma0[0];
		this.sigma[0] = (std>0)?std:-1.0;
	}

	public void setStdY(double std) {
		this.sigma0[1] = (this.sigma0[1] <= 0 && std>0)?std:this.sigma0[1];
		this.sigma[1] = (std>0)?std:-1.0;
	}

	public void setStdZ(double std) {
		this.sigma0[this.getDimension()-1] = (this.sigma0[this.getDimension()-1] <= 0 && std>0)?std:this.sigma0[this.getDimension()-1];
		this.sigma[this.getDimension()-1] = (std>0)?std:-1.0;
	}

	public double getStdX() {
		return this.sigma[0];
	}

	public double getStdY() {
		return this.sigma[1];
	}

	public double getStdZ(){
		return this.sigma[this.getDimension()-1];
	}

	public double getStdXApriori() {
		return this.sigma0[0];
	}

	public double getStdYApriori() {
		return this.sigma0[1];
	}

	public double getStdZApriori(){
		return this.sigma0[this.getDimension()-1];
	}

	public void setStdXApriori(double sigmaX) {
		if (sigmaX > 0)
			this.sigma0[0] = sigmaX;
	}

	public void setStdYApriori(double sigmaY) {
		if (sigmaY > 0)
			this.sigma0[1] = sigmaY;
	}

	public void setStdZApriori(double sigmaZ){
		if (sigmaZ > 0)
			this.sigma0[this.getDimension()-1] = sigmaZ;
	}

	public void setX(double x) {
		this.coordinates[0] = x;
	}

	public void setY(double y) {
		this.coordinates[1] = y;
	}

	public void setZ(double z) {
		//this.coordinates[this.getDimension()-1] = z;
		this.coordinates[2] = z;
	}

	public double getX() {
		return this.coordinates[0];
	}

	public double getY() {
		return this.coordinates[1];
	}

	public double getZ() {
		//return this.coordinates[this.getDimension()-1];
		return this.coordinates[2];
	}

	public double getX0() {
		return this.coordinates0[0];
	}

	public double getY0() {
		return this.coordinates0[1];
	}

	public double getZ0() {
		//return this.coordinates0[this.getDimension()-1];
		return this.coordinates0[2];
	}

	public double getDistance3D(Point p) {
	    return Math.sqrt( Math.pow(this.getX()-p.getX(),2)
	                    + Math.pow(this.getY()-p.getY(),2)
	                    + Math.pow(this.getZ()-p.getZ(),2));
	}

	public double getAprioriDistance3D(Point p) {
	    return Math.sqrt( Math.pow(this.getX0()-p.getX0(),2)
	                    + Math.pow(this.getY0()-p.getY0(),2)
	                    + Math.pow(this.getZ0()-p.getZ0(),2));
	}

	public double getDistance2D(Point p){
		return Math.hypot(this.getX()-p.getX(), this.getY()-p.getY());
	}

	public double getAprioriDistance2D(Point p){
		return Math.hypot(this.getX0()-p.getX0(), this.getY0()-p.getY0());
	}

	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}

	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}

	public void setInfluenceOnNetworkDistortion(double efsp) {
		this.efsp = efsp;
	}

	public double getInfluenceOnNetworkDistortion() {
		return this.efsp;
	}

	public void setNablaCoVarNabla(double ncn) {
		if (ncn>=0)
			this.nablaCoVarNable = ncn;
	}

	public void calcStochasticParameters(double sigma2apost, int redundancy) {
		// Bestimmung der Testgroessen
		double omega = sigma2apost*(double)redundancy;
		int dim = this.getDimension();
		double sigma2apostPoint = (redundancy-dim)>0?(omega-this.nablaCoVarNable)/(redundancy-dim):0.0;
		
		this.Tprio = this.nablaCoVarNable/dim;
		this.Tpost = (sigma2apostPoint > Point.ZERO)?this.nablaCoVarNable/(dim*sigma2apostPoint):0.0;

		// Berechnung der Standardabweichungen
		for (int i=0; i<this.sigma.length; i++) {
			if (sigma[i]>0)
				this.sigma[i] = Math.sqrt(sigma2apost*this.sigma[i]*this.sigma[i]);
		}
	}

	public void setProbabilityValues(double pPrio, double pPost) {
		this.Pprio = pPrio;
		this.Ppost = pPost;
	}

	public double getTprio() {
		return this.Tprio<Point.ZERO?0.0:this.Tprio;
	}

	public double getTpost() {
		return this.Tpost<Point.ZERO?0.0:this.Tpost;
	}

	public double getPprio() {
		return this.Pprio;
	}

	public double getPpost() {
		return this.Ppost;
	}

	public void setInfluencesOnPointPosition(double[] ep) {
		if (ep.length == this.getDimension())
			this.ep = ep;
	}

	public double getInfluenceOnPointPositionX() {
		return this.ep[0];
	}

	public double getInfluenceOnPointPositionY() {
		return this.ep[1];
	}

	public double getInfluenceOnPointPositionZ() {
		return this.ep[this.getDimension()-1];
	}

	public void setFirstPrincipalComponents(double[] principalComponents) {
		if (principalComponents.length == this.getDimension())
			this.principalComponents = principalComponents;
	}

	public double getFirstPrincipalComponentX() {
		return this.principalComponents[0];
	}

	public double getFirstPrincipalComponentY() {
		return this.principalComponents[1];
	}

	public double getFirstPrincipalComponentZ() {
		return this.principalComponents[this.getDimension()-1];
	}

	public void setRedundancy(double[] redundancy) {
		if (redundancy.length == this.getDimension())
			this.redundancy = redundancy;
	}

	public double getRedundancy() {
		double r = 0.0;
		for (int i=0; i<this.redundancy.length; i++)
			r += this.redundancy[i];
		return r;
	}

	public double getRedundancyX() {
		return this.redundancy[0];
	}

	public double getRedundancyY() {
		return this.redundancy[1];
	}

	public double getRedundancyZ() {
		return this.redundancy[this.getDimension()-1];
	}

	public double getGrossErrorX() {
		return this.nabla[0];
	}	

	public double getGrossErrorY() {
		return this.nabla[1];
	}	

	public double getGrossErrorZ() {
		return this.nabla[this.getDimension()-1];
	}		

	public void setGrossErrors(double[] nabla) {
		if (nabla.length == this.getDimension())
			this.nabla = nabla;
	}

	public void setMinimalDetectableBiases(double[] grzw) {
		if (grzw.length == this.getDimension())
			this.grzw = grzw;
	}

	public double getMinimalDetectableBiasX() {
		return this.grzw[0];
	}	

	public double getMinimalDetectableBiasY() {
		return this.grzw[1];
	}	

	public double getMinimalDetectableBiasZ() {
		return this.grzw[this.getDimension()-1];
	}

	public void setOmega(double omega) {
		this.omega = omega;
	}

	public double getOmega() {
		return this.omega;
	}

	public double getConfidenceAxis(int i) {
		return this.confidenceAxis[i];
	}

	public double getConfidenceAngle(int i) {
		return this.confidenceAngles[i];
	}

	public double getConfidenceAxis2D(int i) {
		return this.confidenceAxis2D[i];
	}

	public double getConfidenceAngle2D() {
		return this.confidenceAngle2D;
	}

	public void setConfidenceRegion(ConfidenceRegion confidence) {
		for (int i=0; i<this.getDimension(); i++) {
			this.confidenceAxis[i]   = confidence.getConfidenceAxis(i);
			
			if (this.getDimension() == 1 && i == 0 || this.getDimension() > 1 && i < 2) 
				this.confidenceAxis2D[i] = confidence.getConfidenceAxis2D(i, false);
			
//			if (this.getDimension() > 1 && i < 2) 
//				this.confidenceAxis2D[i] = confidence.getConfidenceAxis2D(i, false);
//			else if (this.getDimension() == 1 && i == 0) // entspricht der Standardabweichung bei einem 1D-Punkt
//				this.confidenceAxis2D[i] = Math.sqrt(Math.abs(confidence.getEigenvalue(i)));
		}
		
		if (this.getDimension() > 1) {
			this.confidenceAngle2D = confidence.getConfidenceAngle2D();
			this.confidenceAngles  = confidence.getEulerAngles();
		}
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}	

	public boolean isSignificant() {
		return this.significant;
	}

	public DeflectionX getDeflectionX() {
		return this.deflectionX;
	}

	public DeflectionY getDeflectionY() {
		return this.deflectionY;
	}

	public void considerDeflection(boolean considerDeflection) {
		this.considerDeflection = considerDeflection;
	}

	public boolean considerDeflection() {
		return this.considerDeflection;
	}

	public boolean hasUnknownDeflectionParameters() {
		return this.considerDeflection && (this.deflectionX.getColInJacobiMatrix() >= 0 && this.deflectionY.getColInJacobiMatrix() >= 0);
	}
}