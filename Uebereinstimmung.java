/*
 * @(#   Uebereinstimmung.java  
 *
 * Copyright (c) 2014, Jean-Michel Lorenzi All Rights Reserved.
 *
 * PDS mit RBN
 */ 
import java.util.*;
import java.lang.Math;
import static dissipativeneuron.Entwicklung.*;                                            

public class Uebereinstimmung extends spectralPDE {
      private int i,j,k;     
      private double eps,  d,  g1;    // convenience notation for parameters of equation 
	  private double  linkeUnruhe,rechteUnruhe,Einfluss; 	  // convenience notation for parameters of equation 
 
	  private boolean dt;//  stattdessen verwendet man get_reaction_dt()  : durch dt_in war es die gleiche
	  
      // Consructor
      public Uebereinstimmung() {
       
            // All the fields in the Constructor are REQUIRED
            fields=2;                   // Number of fields
            spectral=true;              // True if FFT plots may be shown (spectral)
            numPlotTypes=4;             // Number of plot types
      // Default plots of first field are made in makePlot and makeFFTPlot
      // methods in spectralPDE.java. These can be overwritten here for fancier
      // plots or plots of other fields.
             
	
            // Parameters of equation
			nParameters=12;
            defaultValues=new String[nParameters];
            labels = new String[nParameters];
            defaultValues[0]="0.1";           labels[0]=" eps ";
            defaultValues[1]="1.";            labels[1]="force";
            defaultValues[2]="1.";            labels[2]="  *  ";
            defaultValues[3]="1.";            labels[3]="  *  ";
            defaultValues[4]="1.";            labels[4]="  *  ";
            defaultValues[5]="0.";            labels[5]="delta";
            defaultValues[6]="0.";            labels[6]="  *  ";
            defaultValues[7]="0.";            labels[7]="  *  ";
            defaultValues[8]="1.";            labels[8]="  g1 ";
            defaultValues[9]="0.0000001";     labels[9]="  l. Unruhe ";
            defaultValues[10]="0.";           labels[10]="  r. Unruhe ";
            defaultValues[11]="0.";           labels[11]="  Einfluß ";
           // defaultValues[12]="0.1";          labels[12]="  dt ";
      }    
	 
   
     // Grab parameters  
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            eps=parameters[0];
            //in NN   =parameters[1];
            //q=parameters[2];
            //c=parameters[3];
            //g=parameters[4];
            d=parameters[5];
            //v=parameters[6];
            //gamma=parameters[7];  
	        g1=parameters[8]; 
			linkeUnruhe=parameters[9]; 
			rechteUnruhe=parameters[10]; 
			Einfluss=parameters[11];  	 		
    }
	
	 
      // Set random initial conditions specified by icp[][]
           public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
		//  
	synchronized(psi){
           double x,rx,ry,wx,wy,amp; 
if(true) 		   //       464864875454868768786      wx=icp[0][2] is a pb!!!;

//   DT FROM HTML MUß 1 SEIN!!!!


         { for(int k=0;k<fields;k++)   
                for(int i=1;i<=nx;i++)   
                    for(int j=1;j<=ny;j++)                                                                   
                        psi[k][i][j]= Math.random()*0.0000000001;
          for(int k=0;k<fields;k++)
                myFFT.trfor(psi[k],nx,ny);
		 for(int k=0;k<fields;k++)   
                for(int i=1;i<=nx;i++)   
                    for(int j=1;j<=ny;j++)                                                                   
                        psi[k][i][j]= Math.random()*0.0000000001;
 
		 }
	else           for(int k=0;k<fields;k++) {  
                wx=icp[k][2];
                wy=icp[k][3];
                amp=icp[k][4];  
                for(int i=1;i<=nx;i++) {
                    x=i*mesh;                       
                    rx=icp[k][5]*(Math.random()-0.5);
                    for(int j=1;j<=ny;j++) { 
                        ry=icp[k][6]*(Math.random()-0.5);                                                                      
                        psi[k][i][j]=amp*Math.cos(wx*x+wy*j*mesh+rx+ry)+
                            icp[k][0]*(Math.random()-0.5)+icp[k][1];
                    }                     
                }  
           }     
          // Zero out zone boundary values      
/*          for(int k=0;k<fields;k++) {
              for(int i=1;i<=nx;i++)
                    psi[k][i][2]=0.;

              for(int j=1;j<=ny;j++)    
                    psi[k][2][j]=0.;
          }         
*/          
          for(int k=0;k<fields;k++)
                myFFT.trfor(psi[k],nx,ny);
        }
      }                            
    
 public void makePlot(double[][][] psir, int plotwidth, int plotheight) {
 
		if(ENTWICKLUNG_MODUS){
		if( plotType<fields)  {
		
		if( plotType<0)  
					throw new AssertionError("plotType muß <0 sein." );
		}
		
		int i,j,k;
              k=0;
              if(plotType!=0 & plotType < fields) k=plotType; 
              if(k != 0) {
                for(j=1;j<=plotheight;j++) {
                    for(i=1;i<=plotwidth;i++) {
                        psir[0][i][j] = psir[k][i][j];
                    }
                }
              }              
			  
			  



              // Zero out zone boundary values      
              for(i=1;i<=plotwidth;i++)
                    psir[0][i][2]=0.;

              for(j=1;j<=plotheight;j++)    
                    psir[0][2][j]=0.;
}
              // Transform to real space and form plot quantities
              myFFT.trbak(psir[0],plotwidth,plotheight);
     } 
   
   
     // Choose pallette for plot: choice may depend on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette () {      
            myPal = new RGBPallette();
     }     

     // Construct multipliers of linear terms in transform equation at wavevector k
     protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=eps-Math.pow(k*k-1,2);
        d[1]=eps-Math.pow(k*k-1,2); 
        return d;
    }
    
    // Form nonlinear term: take field psip[][][] and construct nonlinear terms psit[][][]
    protected void formNonlinearTerm() {
       int i,j;
       double p;
       
              // Nonlinearity         -psi^3 + g1*psi^2
       for(j=1;j<=ny;j++) {
          for(i=1;i<=nx;i++) {         
             psit[0][i][j] =  - Math.pow(psip[0][i][j],3)-d*Math.pow(psip[1][i][j],2)*psip[0][i][j]
                                     +g1*Math.pow(psip[0][i][j],2);
             psit[1][i][j] =  - Math.pow(psip[1][i][j],3)-d*Math.pow(psip[0][i][j],2)*psip[1][i][j]
                                     +g1*Math.pow(psip[1][i][j],2); 
 
         
          }
       }                    
    }
	
	
				
							
			static boolean gemacht=false;				
							 
	   //  Das ist der unruhige Schritt  .   
    public double tstep(double[][][] psi, double t,boolean Es_gibt_eine_Reaktion) { 
				 synchronized(psi){ 
				
				
		  double time=t;
          for(int k=0;k<fields;k++)
                myFFT.trbak(psi[k],nx,ny);
				
				// mischen  
				double Weite= 1;
				if(true)
                for(k=0;k<fields ;k++){
					double Anteil ; 
					switch(k){ 
					case 0:
						Anteil=linkeUnruhe;
						break;
					case 1: 
						default: 
						Anteil=rechteUnruhe;
					} 
							
					for(j=1;j<=ny;j++)  
						for(i=1;i<=nx;i++)  
							psi[k][i][j]+= Weite*(Math.random()-0.5)*Anteil;
				}     
				double wirklicherEingfluss=  	Einfluss;
				
	 
				for(k=1;k<2;k++)
				 for(j=1;(double)(j)<=(double)(ny)*.7;j+=1 )  
					for(i=1;i<=nx;i++)  
                          { 
							psi[k][i][j]=psi[k][i][j]*(1-wirklicherEingfluss)+ psi[0][i][j]*wirklicherEingfluss;
				          }     
			
			
          for(int k=0;k<fields;k++)
                myFFT.trfor(psi[k],nx,ny);
							
		if(Es_gibt_eine_Reaktion) 				
		return super.tstep (psi,time); 
		else	   {   //  Das ist der unruhige Schritt  .    
				 synchronized(psi){ 
			//	System.out.println("Es gibt keine  Reaktion!");
				 
				// mischen  
				   Weite= 1; 
				 if(!gemacht)
				{gemacht=true;
					for(j=12;j< 22+0*ny;j++)  
						for(i=1;i< 10+0*nx;i+=3)  
							psi[0][i][j] = Weite*(Math.random()-0.5) ;;
					for(j=1 ;j<  ny;j++)  
						for(i=1;i< 10+0*nx;i+=3)  
							psi[1][i][j] = 0 ;
					} 
				  
	  
				 if(false){
          for(int k=0;k<fields;k++)
                myFFT.trbak(psi[k],nx,ny);
          for(int k=0;k<fields;k++)
                myFFT.trfor(psi[k],nx,ny);
				}
      t = t + Zellnetz.get_reaction_dt();//"dt"? anyway it is the same
      return t;
  }
  }
  
  }
}
  
   
   
  
}

/*
 * @(#) Schnackenberg .java  1.5 98/11/5
 *
 * Copyright (c) 1998, Michael Cross All Rights Reserved.
 *
 * Time stepper for Complex Ginzburg Landau Equation using pseudo-spectral method
 */


class Schnackenberg extends spectralPDE {
      private double a,b,d1,d2;
      private int i,j;
      
      // Consructor
      public Schnackenberg() {
            spectral=false;
            nParameters=4;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.1";
            defaultValues[1]="1.5";
            defaultValues[2]="1.";
            defaultValues[3]="20.";
            labels = new String[nParameters];
            labels[0]=" a ";   
            labels[1]=" b ";                  
            labels[2]=" d1";   
            labels[3]=" d2";
            fields=2;
            numPlotTypes=2;   
            
      }     
     
     public void setParameters(int nParameters, double[] parameter) {            
            super.setParameters(nParameters, parameters);
            a=parameter[0];
            b=parameter[1];
            d1=parameter[2];
            d2=parameter[3];
     }
          
     // Forms plot from two fields depending on plotType
//     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {
//
//          super.makePlot(psir, plotwidth, plotheight);
//          if(plotType==1) {
//            for(j=1;j<=plotheight;j++) {
//                  for(i=1;i<=plotwidth;i++) {
//                     psir[0][i][j] = psir[1][i][j];
//                  }
//            }
//          }                    
//     }

     protected double[] getArg(double k) {
        double[] d=new double[fields];
        double b_minus_a_over_b_plus_a=(b-a)/(b+a);
        double a_plus_b_sq=Math.pow(a+b,2);

        d[0] = b_minus_a_over_b_plus_a - d1*k*k;
        d[1] = -a_plus_b_sq - d2*k*k;
        return d;
    }
    
    protected void formNonlinearTerm() {
       int i,j;
       double nonlinear;
       double two_b_over_a_plus_b=2.*b/(a+b);
       double b_over_a_plus_b_sq=b/(Math.pow(a+b,2));             
       double a_plus_b_sq=Math.pow(a+b,2);
       double two_a_plus_b=2.*(a+b);
       
       for(j=1;j<=ny;j++) {
          for(i=1;i<=nx;i++) {
              nonlinear = b_over_a_plus_b_sq*Math.pow(psip[0][i][j],2)
                   + two_a_plus_b*psip[0][i][j]*psip[1][i][j]
                   + Math.pow(psip[0][i][j],2)*psip[1][i][j];
              psit[0][i][j] = nonlinear + a_plus_b_sq*psip[1][i][j]; 
              psit[1][i][j] = -nonlinear - two_b_over_a_plus_b*psip[0][i][j];
          }
       }                  
    }        
     
     public void setPallette() {      
          myPal = new greyPallette();
     }             
} 



/*
 * @(#)SH.java  5/1/99
 *
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 *
 * Time stepper for Swift-Hohenberg Equation using pseudo-spectral method
 * NB: extends spectralPDE
 */ 

class SH extends spectralPDE {
      private int i,j,k;     
      private double eps,g1;            // convenience notation for parameters of equation 
            
      // Consructor
      public SH() {
      
            // All the fields in the Constructor are REQUIRED
            fields=1;                   // Number of fields
            spectral=true;              // True if FFT plots may be shown (spectral)
            numPlotTypes=1;             // Number of plot types
      // Default plots of first field are made in makePlot and makeFFTPlot
      // methods in spectralPDE.java. These can be overwritten here for fancier
      // plots or plots of other fields.
            
            // Parameters of equation
            nParameters=2; 
            nInitialParameters=7; 
            defaultValues=new String[nParameters];
            defaultValues[0]="0.25";
            defaultValues[1]="0.";
            labels = new String[nParameters];
            labels[0]=" eps ";
            labels[1]="  g1 ";             
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            eps=parameters[0];
            g1=parameters[1];
     }
     
     // Choose pallette for plot: choice may depend on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette () {      
            myPal = new RGBPallette();
     }     

     // Construct multipliers of linear terms in transform equation at wavevector k
     protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=eps-Math.pow(k*k-1,2);
        return d;
    }
    
    // Form nonlinear term: take field psip[][][] and construct nonlinear terms psip[][][]
    protected void formNonlinearTerm() {
       int i,j;
       double p;
       
       // Nonlinearity -psi^3 + g1*psi^2
       for(j=1;j<=ny;j++) {
          for(i=1;i<=nx;i++) {
             psit[0][i][j] =- Math.pow(psip[0][i][j],3)+g1*Math.pow(psip[0][i][j],2);
          }
       }                    
    }
    
      // Set random initial conditions specified by icp[][]
           public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
           double x,rx,ry,wx,wy,amp;           
           for(int k=0;k<fields;k++) { 
                wx=icp[k][2];
                wy=icp[k][3];
                amp=icp[k][4];  
                for(int i=1;i<=nx;i++) {
                    x=i*mesh;                       
                    rx=icp[k][5]*(Math.random()-0.5);
                    for(int j=1;j<=ny;j++) { 
                        ry=icp[k][6]*(Math.random()-0.5);                                                                      
                        psi[k][i][j]=amp*Math.cos(wx*x+wy*j*mesh+rx+ry)+
                            icp[k][0]*(Math.random()-0.5)+icp[k][1];
                    }                     
                }  
           }     
          // Zero out zone boundary values      
/*          for(int k=0;k<fields;k++) {
              for(int i=1;i<=nx;i++)
                    psi[k][i][2]=0.;

              for(int j=1;j<=ny;j++)    
                    psi[k][2][j]=0.;
          }         
*/          
          for(int k=0;k<fields;k++)
                myFFT.trfor(psi[k],nx,ny);
                   
      }                            
               
}


/*
 * @(#)SH2.java  7/30/01
 *
 * Copyright (c) 2001, Michael Cross All Rights Reserved.
 *
 * Time stepper for 2 coupled Swift-Hohenberg Equations
 * using pseudo-spectral method
 * NB: extends spectralPDE
 */ 

 class SH2 extends spectralPDE {
      private int i,j,k;     
      private double eps,D,q,c,g,d,v,gamma;            // convenience notation for parameters of equation 
            
      // Consructor
      public SH2() {
      
            // All the fields in the Constructor are REQUIRED
            fields=2;                   // Number of fields
            spectral=true;              // True if FFT plots may be shown (spectral)
            numPlotTypes=2;             // Number of plot types
      // Default plots of first field are made in makePlot and makeFFTPlot
      // methods in spectralPDE.java. These can be overwritten here for fancier
      // plots or plots of other fields.
            
            // Parameters of equation
            nParameters=8;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.3";
            defaultValues[1]="1.";
            defaultValues[2]="1.";
            defaultValues[3]="1.";
            defaultValues[4]="1.";
            defaultValues[5]="1.";
            defaultValues[6]="0.";
            defaultValues[7]="0.";
            labels = new String[nParameters];
            labels[0]=" eps ";
            labels[1]="force";
            labels[2]="  q  ";
            labels[3]="  c  ";
            labels[4]="  g  ";
            labels[5]="delta";
            labels[6]="  v  ";
            labels[7]="gamma";
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            eps=parameters[0];
            D=parameters[1];
            q=parameters[2];
            c=parameters[3];
            g=parameters[4];
            d=parameters[5];
            v=parameters[6];
            gamma=parameters[7];           
     }
     
     // Choose pallette for plot: choice may depend on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette () {      
            myPal = new RGBPallette();
     }     

     // Construct multipliers of linear terms in transform equation at wavevector k
     protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=eps-Math.pow(k*k-1,2);
        d[1]=(eps-D)-Math.pow(k*k-q*q,2);
        return d;
    }
    
    // Form nonlinear term: take field psip[][][] and construct nonlinear terms psip[][][]
    protected void formNonlinearTerm() {
       int i,j;
       double p;
       
       // Nonlinearity 
       for(j=1;j<=ny;j++) {
          for(i=1;i<=nx;i++) {
             psit[0][i][j] =  - Math.pow(psip[0][i][j],3)-d*Math.pow(psip[1][i][j],2)*psip[0][i][j]
                                    + 2*gamma*psip[0][i][j]*psip[1][i][j];
             psit[1][i][j] =- g*Math.pow(psip[1][i][j],3)-d*Math.pow(psip[0][i][j],2)*psip[1][i][j]
                                    + v*Math.pow(psip[1][i][j],2)+gamma*Math.pow(psip[0][i][j],2);
          }
       }              
    }
               
} 
 


 
 
 
 