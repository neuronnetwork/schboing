 /*
 * @(#spectralPDE.java  just a few comments have been added
** and PDE.java  
 *
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 *
 */
 
import static dissipativeneuron.Entwicklung.*;      

public class spectralPDE extends PDE {

      protected double[][][] psit,psip,psinl;
      protected double[][] rmodk;
      protected double[] arg;      

      protected FFT2D myFFT;	  
            
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  double mesh, int in_plotType) {
                  
            // Most intialization done in PDE.class
            super.init(width, height, in_parameters, in_dt, mesh, in_plotType);
                        
            psit = new double[fields][nx+1][ny+1]; 
            psip = new double[fields][nx+1][ny+1];
            psinl = new double[fields][nx+1][ny+1];
            
            arg=new double[fields];
            
            myFFT=new FFT2D();
            init_k(nx,ny,mesh);                                    
      }     
      
      // Set random initial conditions specified by icp[][]
      // Can override for other i.c. if desired
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {

          for(int j=1;j<=ny;j++)
            for(int i=1;i<=nx;i++)
                for(int k=0;k<fields;k++)
                   psi[k][i][j]=icp[k][0]*(Math.random()-0.5)+icp[k][1];
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

	    
      // Default time stepper. Assumes linear terms are diagonal with k dependent
      // codfficients specified by getArg() in function class
      public double tstep(double[][][] psi, double t) {//psi = nicht in Fourier space
            double rmk,efac,oadt,adt;
            double c1=0;
            double c2=0;
            double tp;
            int i,j,k;     
            
     
//    method based on implicit integration 
//    of linear fourier modes
    
//    compute nonlinear contribution to time derivative
     
            tderiv(psi);//psi->Fourier vorwärts->psi
     
//    save the nonlinear term for the corrector
     
//            for(j = 1;j<=ny;j++)
//               for(i = 1;i<=nx;i++)
//                  for(k=0;k<fields;k++)
//                    psinl[k][i][j] = psit[k][i][j];

            for(k=0;k<fields;k++)
                for(i = 1;i<=nx;i++)
                    System.arraycopy(psit[k][i],1,psinl[k][i],1,ny);
                    
            for(j = 1;j<=ny;j++) {
               for(i=1;i<=nx;i++) {
                  rmk = rmodk[i][j];
                  arg=getArg(rmk);
                  for(k=0;k<fields;k++) {
                      efac = Math.exp(arg[k]*dt);
                      psi[k][i][j] = efac*psi[k][i][j] + psit[k][i][j]/arg[k]*(efac-1.0);
                  }    
               }
            }

            tp = t + dt;
            tderiv(psi);//psi->Fourier vorwärts->psi
 
            for(j=1;j<=ny;j++) {
               for(i=1;i<=nx;i++) {
                  rmk = rmodk[i][j];
                  arg = getArg(rmk);
                  for(k=0;k<fields;k++) {
                      adt = arg[k]*dt;
                      oadt = 1.0/adt;
                      efac = Math.exp(adt);
                      c1 = psinl[k][i][j];
                      c2 = psit[k][i][j]-c1;
                      psi[k][i][j] = psi[k][i][j] + dt*c2*oadt*(efac*oadt - oadt - 1.0);                      
                    }
               }
            }
            t = t + dt;
            return t;
      }

      //Dummy routine for coefficients of linear terms (assumed diagonal)
      protected double[] getArg(double k) {
          double[] d=new double[fields];
          return d;
      }               
            
      // Default routine for computing nonlinear term.
      // Assumes only dependence of psi (not derivatives) specified by formNonlinearTerm
      // in function class (psip->psit)    
      protected void tderiv (double[][][] psi) {//psi->Fourier vorwärts->psi
            
            int i,j,k;
            // Make Fourier space copy psip and invert
            
            for(k=0;k<fields;k++) {
                for(i=1;i<=nx;i++) {
//                 for(j=1;j<=ny;j++) {
//                    psip[k][i][j] = psi[k][i][j];                    
//                 }
                   System.arraycopy(psi[k][i],1,psip[k][i],1,ny);
                }
                myFFT.trbak (psip[k],nx,ny);                
            }
            // Get nonlinear term for specific equation
            formNonlinearTerm(); //psip-->psit

            // Return result to Fourier space
            for(k=0;k<fields;k++)
                myFFT.trfor(psit[k],nx,ny);

//          dealias the result (not implemented)
//          dealias(nx,ny,3,psit);

            return;
     }
     
     // Dummy routine to be over-ridden by specific equations
     protected void formNonlinearTerm() {};
          
     // Forms plot from first field.
     // May be overridden with plot depending in plotType
//     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {//Fraum->nFraum
//          int i,j,k;
//          for(k=0;k<fields;k++) {
              // Zero out zone boundary values      
//              for(i=1;i<=plotwidth;i++)
//                    psir[k][i][2]=0.;
//
//              for(j=1;j<=plotheight;j++)    
//                    psir[k][2][j]=0.;
//
              // Transform to real space and form plot quantities
//              myFFT.trbak(psir[k],plotwidth,plotheight);
//          }
//     }
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {//Fraum->nFraum
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

              // Transform to real space and form plot quantities
              myFFT.trbak(psir[0],plotwidth,plotheight);
     }
     
     // Forms FFT plot of first of fields
     // May be overridden with plot depending in plotType
     public void makeFFTPlot(double[][][] psir, int plotwidth, int plotheight) {
          int i,j,k;
          int i1,j1,ix,iy;
          double[][] temp=new double[plotwidth+1][plotheight+1];
              k=0;
              if(plotType!=0 & plotType < fields) k=plotType;          
              // Zero out zone boundary values      
              for(i=1;i<=plotwidth;i++)
                    psir[k][i][2]=0.;    

              for(j=1;j<=plotheight;j++) {
                    psir[k][2][j]=0.;
              }
                                    
          for(i=1;i<=plotwidth;i++)
            for(j=1;j<=plotheight;j++)
                  temp[i][j]=0.;


          for(i=1;i<=plotwidth/2;i++) {
//            System.out.println("There is a IBM JIT bug here");              
              for(j=1;j<=plotheight/2;j++) {                  
                  temp[plotwidth/2+i-1][plotheight/2+j-1]=                                                               
                                Math.pow(psir[k][2*i-1][2*j-1]-psir[k][2*i][2*j],2)
                              + Math.pow(psir[k][2*i-1][2*j]+psir[k][2*i][2*j-1],2);
                  temp[plotwidth/2-i+1][plotheight/2-j+1]=
                                temp[plotwidth/2+i-1][plotheight/2+j-1];
                  temp[plotwidth/2-i+1][plotheight/2+j-1]=   
                              + Math.pow(psir[k][2*i-1][2*j-1]+psir[k][2*i][2*j],2)
                              + Math.pow(psir[k][2*i-1][2*j]-psir[k][2*i][2*j-1],2);
                  temp[plotwidth/2+i-1][plotheight/2-j+1]=
                                temp[plotwidth/2-i+1][plotheight/2+j-1];
              }
          }

                              
          for(i=0,i1=0;i<plotwidth/(2*scalex);i++) {
              for(ix=0;ix<scalex;ix++,i1++) {
                for(j=0,j1=0;j<plotheight/(2*scaley);j++) {
                  for(iy=0;iy<scaley;iy++,j1++) {
                        psir[0][plotwidth/2+i1][plotheight/2+j1]=
                            Math.sqrt(temp[plotwidth/2+i][plotheight/2+j]);                              
                        psir[0][plotwidth/2-i1][plotheight/2+j1]=
                            Math.sqrt(temp[plotwidth/2-i][plotheight/2+j]);                              
                        psir[0][plotwidth/2+i1][plotheight/2-j1]=
                            Math.sqrt(temp[plotwidth/2+i][plotheight/2-j]);                              
                        psir[0][plotwidth/2-i1][plotheight/2-j1]=
                            Math.sqrt(temp[plotwidth/2-i][plotheight/2-j]);                              
                  }
                }
              }
           }
                      
     }                 
          
//    Set up complicated array of mod k for efficiency
      private void init_k(int nx, int ny, double mesh) {
	       
          double alpha = 2.0*Math.PI/(nx*mesh);
          double beta = 2.0*Math.PI/(ny*mesh);
          double kx,ky;
          int i,j,k;
          rmodk = new double[nx+1][ny+1];
     
          for(j=1;j<=ny;j++) {
             ky = (j-1)/2;
             if ( j==1) ky = 0;
             if ( j== 2) ky = ny/2;
             for(i=1;i<=nx;i++) {
                kx = (i-1)/2;
                if ( i== 1) kx = 0;
                if ( i== 2) kx = nx/2;
                rmodk[i][j] = Math.sqrt(Math.pow(alpha*kx,2) + Math.pow(beta*ky,2));
             }
      }                      
}
           }


























                                                            
  
 abstract class PDE {
      protected double[] parameters;
      
      public int nParameters;
      public int nInitialParameters=2;
      public int fields;
      public String[] defaultValues;
      public String[] labels;
      public boolean spectral;
      public int numPlotTypes=1;
      
      protected int scalex;
      protected int scaley;
      protected int nx,ny;
      protected double dt;
      protected double mesh;
      protected int plotType;
      protected myPallette myPal;                 // Pallette for plotting
                  
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  double in_mesh, int in_plotType) {
                  
            nx = width;
            ny = height;                       
            
            parameters = new double[nParameters];
            for(int i=1;i<nParameters;i++) 
                parameters[i]=in_parameters[i];            
            
            dt=in_dt;
            mesh=in_mesh;
            plotType=in_plotType;
            setPallette();
      }     
      
      // Set initial conditions specified by icp[][]
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
      }                               

      // Dummy time stepper. 
      public abstract double tstep(double[][][] psi, double t) ;
	      public double tstep(double[][][] psi, double t,boolean KeineReaktion){ 
		if(2+2==4)return  tstep(psi, t);
		if(2+2==4)System.out.println(" tstep(double[][][] psi, double t,boolean KeineReaktion)");
		if(2+2==4)throw new AssertionError("me!");
		return t;	}  

     // Forms plot from two fields depending on plotType: to be over-ridden
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {
     }

     // Forms plot from two fields depending on plotType: to be over-ridden
     public void makeFFTPlot(double[][][] psir, int plotwidth, int plotheight) {
     }
     
     public void setParameters(int nParameters, double[] in_parameters) {            
            for(int i=0;i<nParameters;i++) 
                parameters[i]=in_parameters[i];                
     }
     
     public myPallette getPallette() {
           return myPal;
     }
     
     public void setPlotType(int in_plotType) {
           plotType=in_plotType;
     }
     
     public void setPallette() {      
           myPal = new myPallette();
     }                       

     public void setPallette(int palletteType) {      
        switch (palletteType) {
             case 0:
                   setPallette();
                   break;
             case 1:
                   myPal = new RGBPallette();
                   break;                   
             case 2:
                   myPal = new greyPallette();
                   break;                        
             case 3:
                   myPal = new circPallette();
                   break;
             case 4:
                   myPal = new myPallette();
                   break;
             default:
                   setPallette();
                   break;
        }
    }
    
    public void setScales(int sx, int sy) {
        scalex=sx;
        scaley=sy;
    }                         
 
  
  }
      
	  
	                                                                     
     /*
 * @(#)FHN.java  1.5 3/4/99
 * Copyright (c) 1999, Michael Cross All Rights Reserved. 
 */ 

class FHN extends fdPDE {
      int i,j;
      double eps;
      double a,D,g,w;      
            
      // Consructor
      public FHN() {
            fields=2;
            nParameters=5;
            numPlotTypes=2;
            spectral=false;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.5";
            defaultValues[1]="0.0";
            defaultValues[2]="1.95";
            defaultValues[3]="0.0";
            defaultValues[4]="0.0";
            labels = new String[nParameters];
            labels[0]="  a ";                  
            labels[1]="  D  ";
            labels[2]=" eps ";
            labels[3]="Gamma";
            labels[4]="omega";
      }     
//here in FHN
      public double tstep(double[][][] psi, double t) {//psi = nicht in Fourier space
     
      int i,j;
      double u_th,temp;
      int ktmp;
      
      /* interchange k and kprm */
      ktmp = kprm;
      kprm = k;
      k = ktmp;
      
      /* main loop */
      for(i=1; i<=nx; i++) {
            for(j=1; j<=ny; j++) {
                  u_th=psi[1][i][j];
                  psi[1][i][j] = psi[1][i][j] + dt * eps * (psi[0][i][j] - a*psi[1][i][j]);                  
                  psi[0][i][j] = psi[0][i][j]
                        +dt*((1.-(1+g*Math.cos(w*t))*psi[0][i][j]*psi[0][i][j])*psi[0][i][j]-u_th)
                         + D * dt * lap[k][i][j]; 

                  lap[kprm][i][j]   = lap[kprm][i][j] - 4.*psi[0][i][j];
                  lap[kprm][i+1][j] = lap[kprm][i+1][j] + psi[0][i][j];
                  lap[kprm][i-1][j] = lap[kprm][i-1][j] + psi[0][i][j];
                  lap[kprm][i][j+1] = lap[kprm][i][j+1] + psi[0][i][j];
                  lap[kprm][i][j-1] = lap[kprm][i][j-1] + psi[0][i][j];
 
                  lap[k][i][j] = 0.;
             }
      }
        
      /* impose no-flux boundary conditions */
      for(i=1; i<=nx; i++) {
            lap[kprm][i][1] = lap[kprm][i][1] + psi[0][i][2];
            lap[kprm][i][ny] = lap[kprm][i][ny] + psi[0][i][ny-1];
      }
      for(j=1; j<=ny; j++) {      
            lap[kprm][1][j] = lap[kprm][1][j] + psi[0][2][j];
            lap[kprm][nx][j] = lap[kprm][nx][j] + psi[0][nx-1][j];
      }
      t = t + dt;
//      System.out.println("k= "+k+" kp= "+kprm+" dt= "+dt+" D= "+D+" dt_o_eps= "+dt_o_eps+" b_o_a= "+
//                b_o_a+" one_o_a= "+one_o_a);

      return t;
      }

     
             
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            a=parameters[0];            
            D=parameters[1];
            eps=parameters[2];
            g=parameters[3];
            w=parameters[4];
     }
          
     // Forms plot from two fields depending on plotType
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {

        if(plotType==1) {  
          for(j=1;j<=plotheight;j++) {
             for(i=1;i<=plotwidth;i++) {
                   psir[0][i][j] = psir[1][i][j];
             }
          }
        }                    
     }
          
     public void setPallette() {      
                  myPal = new RGBPallette();
     }             
}
 

/*
 * @(#fdPDE.java  5/7/99
 *
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 *
 * Stub code for time stepper for finite difference PDE 
 */
 
class fdPDE extends PDE {
      protected double[][][] lap;
      protected int k,kprm;
      
      // Most of intializataion done in PDE.java
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  double in_mesh, int in_plotType) {
                  
            super.init(width, height, in_parameters, in_dt, in_mesh, in_plotType);
            
            nInitialParameters=2;
                        
            spectral=false;
            lap = new double[2][nx+2][ny+2];
            for(int m=0;m<2;m++)
               for(int i=0;i<nx+2;i++)
                   for(int j=0;j<ny+2;j++)
                        lap[m][i][j]=0.;
            
            k=0;
            kprm=1;            
      }     

      public double tstep(double[][][] psi, double t) {
            return t;
      }
     
      // Set random initial conditions specified by ic[][]
      // Can override for other i.c. if desired
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
        for(int j=1;j<=ny;j++) {
            for(int i=1;i<=nx;i++) {
                for(int k=0;k<fields;k++) {
                   psi[k][i][j]=icp[k][0]*(Math.random()-0.5)+icp[k][1];
                }   
            }
        }
      }  
}

  

// Sets up default grey scale pallette for plotting
 class myPallette {
          public int[] r,g,b;
          public myPallette() {
                r = new int[256];
                g = new int[256];
                b = new int[256];
                for(int i=0;i<256;i++) {
                  r[i]=i;
                  g[i]=i;
                  b[i]=i;
                }
           
}          
}

// Sets up circular rainbow pallette for phase plot
 class RGBPallette extends myPallette {
      
      public RGBPallette() {
            int i,j;
            for(j=0,i=0;i<128;i++,j++) {
                  b[j]=255-2*i*i/128;
                  g[j]=255-2*(128-i)*(128-i)/128;
                  r[j]=0;
            }
            for(i=0;i<128;i++,j++) {
                  b[j]=0;
                  g[j]=255-2*(i)*(i)/128;
                  r[j]=255-2*(128-i)*(128-i)/128;
            }
            
            // Check range for mistakes!
            for(i=0;i<=255;i++) {
               if(r[i]<0) r[i]=0;
               if(g[i]<0) g[i]=0;
               if(b[i]<0) b[i]=0;
               if(r[i]>255) r[i]=255;
               if(g[i]>255) g[i]=255;               
               if(b[i]>255) b[i]=255;
            }    
      }
}                  
        

// Sets up default grey scale pallette for plotting
 class greyPallette extends myPallette {
          public int[] r,g,b;
          public greyPallette() {
                r = new int[256];
                g = new int[256];
                b = new int[256];
                for(int i=0;i<256;i++) {
                  r[i]=i;
                  g[i]=i;
                  b[i]=i;
                }
          }
}            


// Sets up circular rainbow pallette for phase plot
 class circPallette extends myPallette {
      
      public circPallette() {
            int i,j;
            double multWide=255./42.;
            double multNarrow=255./41.;
            for(j=0,i=0;i<42;i++,j++) {
                  r[j]=255;
                  g[j]=0;
                  b[j]=(int)(multNarrow*i);
            }
            for(i=0;i<43;i++,j++) {
                  r[j]=255-(int)(multWide*i);
                  g[j]=0;
                  b[j]=255;
            }
            for(i=0;i<43;i++,j++) {
                  r[j]=0;
                  g[j]=(int)(multWide*i);
                  b[j]=255;
            }
            for(i=0;i<43;i++,j++) {
                  r[j]=0;
                  g[j]=255;
                  b[j]=255-(int)(multWide*i);
            }                        
            for(i=0;i<43;i++,j++) {
                  r[j]=(int)(multWide*i);
                  g[j]=255;
                  b[j]=0;
            }      
            for(i=0;i<42;i++,j++) {
                  r[j]=255;
                  g[j]=255-(int)(multNarrow*i);
                  b[j]=0;
            }      
            
            // Check range for mistakes!
            for(i=0;i<=255;i++) {
               if(r[i]<0) r[i]=0;
               if(g[i]<0) g[i]=0;
               if(b[i]<0) b[i]=0;
               if(r[i]>255) r[i]=255;
               if(g[i]>255) g[i]=255;               
               if(b[i]>255) b[i]=255;
            }    
      }
   
                                      
}                  

  


