
/*
 * @(#)Zellnetz.java  
 *
 * Copyright (c) 2014, Jean-Michel lorenzi All Rights Reserved. 
*/



import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JPanel; 
import javax.swing.JButton; 
import javax.swing.JApplet; 
import javax.swing.JFrame; 
import java.applet.Applet;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.lang.InterruptedException;
import java.io.*;
import java.net.*;  
import static dissipativeneuron.Entwicklung.*; 
//import org.math.plot.Plot2DPanel;  

    
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

 
public class Zellnetz extends  evolvePDE {
	 static
		{
		try {System.runFinalizersOnExit(true);}
		catch(java.lang.SecurityException t)
			{
			// java.lang.SecurityException: runFinalizersOnExit
			// at java.lang.Runtime.runFinalizersOnExit(Unknown Source)
			// at java.lang.System.runFinalizersOnExit(Unknown Source)
			// at Zellnetz.init(Zellnetz.java:93)
			// at sun.applet.AppletPanel.run(Unknown Source)
			// at java.lang.Thread.run(Unknown Source)
			}
		}
	private static final int UE=9;
	public final boolean  APPLET;
	int     ZahlVonPds;   // Number of planedissipativestructures
	public Pfeiler[] diePfeiler;
	private NN[] nnp;
	NN nimmNN(){	
	return nnp[0];
	}
	protected java.awt.Container frame_or_applet;
	private boolean dt;  //verbietet unabsichtlichte Verwendung von super.dt oder Zellnetz.get_dt()

    znPlotChoiceControls plotChoice;
	boolean runAnimation;
	
	public Zellnetz(){ 
		this(true);
		}
	public Zellnetz(boolean applet){ {
		APPLET = applet;
		ZahlVonPds=10;                   // Number of  
		diePfeiler =  new Pfeiler[ZahlVonPds]; 
		for (int pdsNummer=0; pdsNummer<ZahlVonPds;pdsNummer++) 
		    diePfeiler[pdsNummer] =  new Pfeiler(ZahlVonPds ,257,257);   
			}
			
		nnp=new NN[1];	  
		nnp[0]=new NN(diePfeiler[0] );	  
		
		nimmNN().rbn=diePfeiler[0];
		nimmNN().rbn.Unruhe(); 
		if((nimmNN().EBM.Breite()>plotwidth) ||(nimmNN().EBM.Breite()>plotwidth) )
			System.out.println("Ich mag nicht, daß Breite() = plotwidth + etwas kleines. um Verbesserung857872 zu verbessern");//throw new AssertionError("");
		
			if(!APPLET)
			frame_or_applet=new Frame("Astro");
		else
			frame_or_applet=this;
		}
	
	public static double get_dt(){ 
		return evolvePDE.dt;//*0.02;
	}
	public static double get_reaction_dt(){ 
		return evolvePDE.dt;
	}
	public Frame get_frame(){
		if(APPLET)
			throw new AssertionError("Benutz \"get_applet()\" stattdessen");
		return (Frame)(Object) frame_or_applet;
	}
	public Applet get_applet(){
		if(!APPLET)
			throw new AssertionError("Benutz \"get_frame()\" stattdessen");
		return (Applet)(Object) frame_or_applet;
	}
	
	
	public void init() {
		if(!APPLET)
			throw new AssertionError("frame . init ()??");
    
        // Initialize function
        function=(new Integer(getFromApplet("function","0"))).intValue();

        switch (function) {
             case CGL:
                   myPDE = new CGL();
                   break;
             case BRUSSELATOR:
                   myPDE = new Brusselator();
                   break;                   
             case SCHNACKENBERG:
                   myPDE = new Schnackenberg();
                   break;                        
             case SH:
                   myPDE = new SH();
                   break;
             case BARKLEY:
                   myPDE = new Barkley();
                   break;
             case BAR:
                   myPDE = new Bar();
                   break;
             case LIFSHITZ:
                   myPDE = new Lifshitz();
                   break;
             case SH2:
                   myPDE = new SH2();
                   break;
             case FHN:
                   myPDE = new FHN();
                   break;    
            default:
                   myPDE = new CGL();
                   break;         
			case UE:
                   myPDE = new Uebereinstimmung();
                   break;
 
 
        }
        
        // Get parameters from html file
        nParameters=myPDE.nParameters;
        nicp=myPDE.nInitialParameters;        
        parameters=new double[nParameters];
        getAllParameters();
        oldPlotType=plotType;
        
        xmin=0.;
        xmax=width*mesh;
        ymin=0.;
        ymax=height*mesh;
                
        // Set pltting scale factors
        if(plotwidth<width) plotwidth=width;
        if(plotheight<height) plotheight=height;
        scalex=plotwidth/width;
        scaley=plotheight/height;
        myPDE.setScales(scalex,scaley);
		if(scalexy==0)
			scalexy=scalex;
        
        // Print out parameters to console
        System.out.println("dt ="+get_reaction_dt()); 
        for(int i=0;i<nParameters;i++)
            System.out.println("parameter"+i+" = "+parameters[i]);
        System.out.println("width ="+width+" height ="+height);
        System.out.println("plotwidth ="+plotwidth+" plotheight ="+plotheight);
        System.out.println("xmin ="+xmin+" xmax ="+xmax+" ymin ="+ymin+" ymax ="+ymax+" ");
                
        // Set up arrays
        fields=myPDE.fields;
        System.out.println("Number of fields= "+fields);
        icp=new double[fields][nicp];        
        for(int i=0;i<fields;i++) {
           for(int j=0;j<nicp;j++) {
              icp[i][j]=(new Double(getFromApplet("ic"+i+j,"0."))).doubleValue();            
              System.out.println("icp["+i+j+"]: "+icp[i][j]);
           }   
        }    
        psi = new double[fields][width+1][height+1];
        psir = new double[fields][plotwidth+1][plotheight+1];
        
// Set up image for animation. See
// http://www.javasoft.com/products/jdk/1.2/docs/api/java/awt/image/MemoryImageSource.html
        pixels = new int[plotwidth * plotheight];
        source=new MemoryImageSource(plotwidth, plotheight,
                  ColorModel.getRGBdefault(), pixels, 0, plotwidth);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);
        image=createImage(source);        
        
        // Set up GUI
        setLayout(new BorderLayout());
		
		set_plotControls(); 
        add("South", plotControls); 
		  
            Panel topPanel = new Panel();
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            minLabel = new Label("Min = -0.00000000",Label.LEFT);
            topPanel.add(minLabel);
            maxLabel = new Label("Max = 0.00000000",Label.LEFT);
            topPanel.add(maxLabel);
            add("Center",topPanel); 
		
        Panel rightPanel = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH; 
        rightPanel.setLayout(gridbag);
        add("East",rightPanel);  
		 // {
			// double[] x =   {0,5,2};
  // double[] y =  {4,5,6};
 // Plot2DPanel Funktion= new Plot2DPanel( );  
  // Funktion.addLinePlot("my plot", x, y); 
  //put the PlotPanel in a JFrame, as a JPanel
  // JFrame frame = new JFrame("a plot panel");
  // if(false)   
        // add("West",Funktion);  
		// else
  // frame.setContentPane(Funktion);
  // frame.setVisible(true);
		// }

        String[] textboxes = new String[nParameters];
        for(int i=0;i<nParameters;i++)
                  textboxes[i]= String.valueOf(parameters[i]);        
        parameterControls = new textControls(textboxes,myPDE.labels,nParameters,5);

        constraints.gridwidth=1;
        constraints.weightx=1;
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        constraints.weighty=4;        
        gridbag.setConstraints(parameterControls, constraints);
if(true)      topPanel.add(parameterControls);    
	else	rightPanel   .add(parameterControls);                     

        {
            plotChoice=new znPlotChoiceControls(myPDE.numPlotTypes,myPDE.spectral);
            constraints.weighty=1;
            gridbag.setConstraints(plotChoice, constraints);                      
            rightPanel.add(plotChoice);
            plotChoice.setPlotType(plotType);        
            if(myPDE.spectral) plotChoice.setPlotFFT(plotFFT);
        }
        
        add("North", canvas = new plotCanvas());    
        if(!scalePlot) 
			canvas.setSizeProgrammer(plotwidth,plotheight); //  sehe setSize
		
        // Set up solver, and plotting
        myPDE.init(width, height,parameters,get_reaction_dt(),mesh,plotType);  
        if(palletteType!=0) myPDE.setPallette(palletteType);
        myPal = myPDE.getPallette();

        
      plotControls.getParams(vals);
      for(int i=0;i<nParameters;i++)
               parameters[i]=parameterControls.parseTextField(i,parameters[i]);
      myPDE.setParameters(nParameters,parameters);        

                
        // Set up initial conditions
        setInitialConditions();
       
        // Start thread for evolving
        appletThreadGroup = Thread.currentThread().getThreadGroup();      
	}
		
	   public void run() {

        while(this.runAnimation) {			 
//            Don't seem to need next line 5/27/99
//            canvas.setImage(null);            // Wipe previous image
              Image img = calculateImage(); //nimmNN().rechnen();  // in Shritt()  in calculate
              synchronized(this) {
                  if (img != null && runner == Thread.currentThread())
                      canvas.setImage(img);   
              }
        }
        plotControls.setStart();       // Set plotControls ready to start again
        if((!file_out.equals("none")) & runOnce) writeout();
    }
	
  
	public void setParameters (int n, double[] vals) { 
		myPDE.setParameters(n,vals);
		nimmNN().setSomeParameters(n,vals);
	}
	
	public void getParams (double[] vals) { 
		getParamsAndButtons( vals);
	}
	
	public void getParamsAndButtons(double[] vals) { 
		plotControls.getParams(vals); 		
	} 

	final protected Image calculateImage() {
	 
//  erforderlich?: synchronized(diePfeiler[0].pds){
		 

      int i,j,k,ist,count;

      // Update parameters from GUI
      getParams (vals);
      count=(int)vals[0];
      for(i=0;i<nParameters;i++)
               parameters[i]=parameterControls.parseTextField(i,parameters[i]);
      setParameters(nParameters,parameters);

      // Evolve equations if not i.c., otherwise get ready
	     
      if(runOnce) {
           ist=0;
           while(ist++<count && this.runAnimation) { 
			Schritt();
			}
	  }             

	 int pdsNummer=7; // for (;pdsNummer<ZAHLVONWIRKLICHENPDS;pdsNummer++)
	  {
	
					
	   if(NN.SCHWARZWPRUEFUNG){ 
	    
		if(ZahlVonPds<9) 
						throw new AssertionError("  ZahlVonPds<9! ");  
		
		if(plotChoice.NNcb.getState() )		
			display(diePfeiler[0].pds[0], pixels );
		else
			die_BM_wurden_gezogen();
	   }
	   else{
	   for(k=0;k<fields;k++) {
          // Interpolate into larger plot range
          if(plotwidth!=width || plotheight!=height)
            for (j = 1; j <= plotheight; j++)
              for (i = 1; i <= plotwidth; i++)
                   psir[k][i][j]=0.; 
          for (j = 1; j <= height; j++) 
              for (i = 1; i <= width; i++) 
                   psir[k][i][j]=diePfeiler[pdsNummer].pds[k][i][j];
      }
      
	  
			if( true){ 
				for (j = 1; j <= height; j++) 
					for (i = 1; i <= width; i++) 
						psir[0][i][j]=diePfeiler[pdsNummer].pds[0][i][j];
			}
      // Type of plot depends on function and user choice
      if(choosePlots) {
        plotType=plotChoice.getPlotType();
        myPDE.setPlotType(plotType);
        if(plotType!=oldPlotType) {
            myPDE.setPallette();
            myPal = myPDE.getPallette();
            oldPlotType=plotType;
        }
		
		
        if(plotChoice.getPlotFFT())
            myPDE.makeFFTPlot(psir,plotwidth,plotheight);
        else
            myPDE.makePlot(psir,plotwidth,plotheight);
      }
      else
         myPDE.makePlot(psir,plotwidth,plotheight);
	
      
      // Scale onto color plot 
      scale(psir[0],pixels,plotwidth,plotheight); 
	  
	  }	 
}
      if(showTime) plotControls.setTime("Time = "+String.valueOf((float) t)); 
	  plotControls. setNn( "TEST = "+String.valueOf(Math.random()*10));
           
      // If this is intial condition get ready to animate
      if(!runOnce) {
         this.runAnimation=false; 
         plotControls.buttonEnable();
         runOnce=true;
      }      

      // Poll once per frame to see if we've been told to stop.
      Thread me = Thread.currentThread();            
      if (runner != me) return null;

      source.newPixels();      
      return image; 

    } 
	
	public void Schritt(){
		double neuerAugenblick=0;
		if(nimmNN().Reaktion) { //  sonst macht den Kreis.  Beim "lzufaellig" klicken wascht sich Alles. 
				for (int pdsNummer=0;pdsNummer<ZahlVonPds;pdsNummer++) 
					  neuerAugenblick=myPDE.tstep(diePfeiler[pdsNummer].pds,t,
					  nimmNN().Reaktion 
					  );  //nichtFRaum ->FourierRauum 
				t=neuerAugenblick;				  
						}
		else
				{	
				nimmNN().rechnen();
				nimmNN().zeichnen();
				}
				
	}
	
	public void stop() {
		runner = null;
        this.runAnimation=false;
		plotControls.setButtonLabel(" Start ");
    }
 	
    // Only works for applet started locally from directory in CLASSPATH
    public void writeout() {
synchronized(diePfeiler[0].pds){
      
      double[] min;
      double[] max;
      
      double[] mult;
      byte outdata;
      int i,j,k;
      
      min=new double[fields];
      max=new double[fields];       
      mult=new double[fields];
      for(k=0;k<fields;k++) {
           min[k]=1000000.;
           max[k]=-1000000.;
      } 
      
     for (int pdsNummer=0;pdsNummer<ZahlVonPds;pdsNummer++)  
      for(i=0;i<width+1;i++){
           for(j=0;j<height+1;j++) {
               for(k=0;k<fields;k++) {
                  min[k] = Math.min(min[k],diePfeiler[pdsNummer].pds[k][i][j]);
                  max[k] = Math.max(max[k],diePfeiler[pdsNummer].pds[k][i][j]);
               }  
           }
       } 

      for(k=0;k<fields;k++) {
          mult[k]=254.9/(max[k]-min[k]);
          System.out.println("Field "+k+" min= "+min[k]+" max= "+max[k]+" mult= "+mult[k]);
      }    

      try {
      DataOutputStream dos = new DataOutputStream(
                                     new FileOutputStream(file_out));

      dos.writeInt(width);
      dos.writeInt(height);
      for(k=0;k<fields;k++) {
         dos.writeDouble(min[k]);
         dos.writeDouble(max[k]);
         dos.writeDouble(mult[k]);
      }     
      
     for (int pdsNummer=0;pdsNummer<ZahlVonPds;pdsNummer++) 
	 	 for(i=0;i<width+1;i++){
            for(j=0;j<height+1;j++) {
                for(k=0;k<fields;k++) {                       
//    If want more accurate write out and read in use writeDouble and then readDouble
//                  dos.writeDouble((mult[k]*(diePfeiler[pdsNummer].pds[k][i][j]-min[k])));
                    dos.writeByte((byte)Math.rint(mult[k]*(diePfeiler[pdsNummer].pds[k][i][j]-min[k])));
                }
            }
       }     
      dos.close();
      } catch (IOException e) {System.out.println("File IO error");
        }                                                  
    }
}

	private static  double[][] data;
  
	protected void display(double[][] ca , int[] pixels ) {
		if(data==null)
			data=new double[plotwidth][plotheight];
		if(!BIGSQUARES)
			for(int i=0 ;i<plotwidth/scalex;i++)  
                for(int j=0 ;j<plotheight/scaley;j++)  
					data[i][j]= ca[ i ][ j ]  ;  
		else
		for(int i=0,i1=0;i<plotwidth/scalex;i++)  
              for(int ix=0;ix<scalex;ix++,i1++)  
                for(int j=0,j1=0;j<plotheight/scaley;j++)  
                  for(int iy=0;iy<scaley;iy++,j1++)  
					data[i1][j1]= ca[ i ][ j ]  ;  
                      
       double min;
       double max;
       double mult;
       int plotdata;
       int c[] = new int[4];
       int index = 0;
       int i,j;
       
	   if(false){
	   min=0;
	   max=1; 
	   }
	   else{
       min=1000000.;
       max=-1000000.;
       if(scaleMinMax || showMinMax) {
           for(j=0;j<plotheight;j++){
               for(i=0;i<plotwidth;i++) {
                   min = Math.min(min,data[i][j]);
                   max = Math.max(max,data[i][j]);
               }
           }
       }       
       if(showMinMax) {
           minLabel.setText("Min = "+String.valueOf((float) min));       
           maxLabel.setText("Max = "+String.valueOf((float) max)); 
       }
	   }	   
                 
       mult=255./(max-min); 
	    
       
       for(j=0;j<plotheight;j++) {
           for(i=0;i<plotwidth;i++){
                plotdata=(int)(mult*(data[i][j]-min));
                if(plotdata<0) throw new AssertionError(" if(plotdata<0)");  
                if(plotdata>255) throw new AssertionError(" if(plotdata >    ");  
                c[0] =  plotdata  ;
                c[1] =  plotdata  ;
                c[2] =  plotdata ;
                c[3] = 255;//Durchsichtigkeit
                pixels[index++] = ((c[3] << 24) |
                           (c[0] << 16) |
                           (c[1] << 8) |
                           (c[2] << 0));                
           }
       }       
    }
 	
	protected void die_BM_wurden_gezogen(){
		//if(plotChoice.EBMcb.getState() )  
		int[][] data ;  
		data=new int [plotwidth][plotheight];
		for(int i=0 ;i<plotwidth;i++) for(int j=0 ;j<plotheight;j++) 				
						data[i][j]=  (255 << 24)  ;//Durchsichtigkeit   
		for(int jj=0;jj<nimmNN().Zahl_von_der_BM;jj++)   
			{									//Für jede Boltzmannmaschine
			int Farbe=(0 << 0)    //blau
					| (0 << 8 )    //rot
					| (0 << 16 ) ;   //grün
					
			switch(jj){
				case 0:
				Farbe|=255 << 0;//blau
				break;
				case 1:
				Farbe|=255 << 8;//rot
				break;
				case 2:
				Farbe|=255 << 16;//grün
				break;
				default:
				Farbe|=(128 << 0)    
					 | (128 << 8 )   
					 | (128 << 16 );
			} 
				
			Stelle[] variables=nimmNN().BM[jj].variables; 
			for(int v=0;v<nimmNN().BM[jj].number_of_variables ;v+=2){  
					
					int x=(int)(double)( variables[v +0].Kommazahl);
					int y=(int)(double)( variables[v +1].Kommazahl);  
					int B=(int)(double)nimmNN().BM[jj].Breite();
					int Bx=B;
					int By=B;
					if(!Verbesserung857872){//sehe auch 546846874684864
						Bx=plotwidth;By=plotheight;
					}else
					if((x<0)||(!(x<Bx))||(y<0)||(!(y<By)))
					                                     throw new AssertionError(x+" "+y+" "+"x<0 oder . ..");
					int sieben=topologischeBoltzmannmaschine.Beispiel().Sache();
					for(int i=0 ;i<sieben;i++)  
							for(int j=0 ;j<sieben;j++) 	
								data[(x+i)%Bx][(y+j)%By]|=Farbe;
							   }		
			} 
			
       int index = 0;
       int i,j;   
       for(j=0;j<plotheight;j++)  
           for(i=0;i<plotwidth;i++){ 
                pixels[index++] = data[i][j];     
               // pixels[index++] = ((255 << 24) |
                           // (((int)(Math.random()*255))<<16) |
                           // (0 << 8) |
                           // (0 << 0));                              
           }

		}
		
protected void set_plotControls(){ 	
        plotControls=new Inner_plot_controls(this, this, steps, showTime);
	}
 
class Inner_plot_controls extends plotControls{ 

	private int runAnimation;//to forbid access: now it is Zellnetz.this.runAnimation
    Button button2,button3,button4,button5,button6,button7,button8;

	public Inner_plot_controls(evolvePDE pde_rein,Applet app,double p1, boolean inShowTime) {
		super(pde_rein,   p1,   inShowTime);
		add(button2=new Button("l zufaellig")); 
		//add(button6=new Button("  tanul ?   "));  
		add(button4=new Button("shake"));  
		//add(button4=new Button(" Reakt. ? "));
		add(button3=new Button("r zufaellig")); 
		add(button5=new Button("Reset")); 
		//add(button8=new Button("Entsp"));  
		Zellnetz.this.runAnimation=true;
		}
		
     // Java 1.0 event handler
    public boolean action(Event evt, Object arg) {  
		super.runAnimation=Zellnetz.this.runAnimation;
		boolean Ergebnis=super.action(evt,   arg);
		Zellnetz.this.runAnimation=super.runAnimation;
		if(Ergebnis)
			return true;
		if(evt.target==button2) {    

           if(Zellnetz.this.runAnimation) 

		   {
			//VERGIß NICHT ES  FALLS ES ERFORDERLICH IST: synchronized(diePfeiler   [0].pds)
			 { 
			  nimmNN().Haut.Unruhe();
			}       		
			return true;
			}
		}
         else if(evt.target==button3) {   

           if(Zellnetz.this.runAnimation) {

			 for(int pdsNummer=0; pdsNummer< ZahlVonPds;pdsNummer++) 
			 synchronized(diePfeiler[pdsNummer].pds){ 
			 diePfeiler[pdsNummer].Unruhe();    
         }       		
			return true;
		}
        }  	 
	else if(evt.target==button4) 
		{  //shake

           if(Zellnetz.this.runAnimation)

			{
			nimmNN().kleinen_Fehler();
			return true;
			}
		} 
	else if(evt.target==button8) 
		{  
			nimmNN().Entspannung=! nimmNN().Entspannung;
			return true;
		}
	else if(evt.target==button6) {  
         }       	 

	return false; 
}	 
}

//************************************************************************    
   protected void setInitialConditions() { 
        t=0.;   
     for (int pdsNummer=0;pdsNummer<ZahlVonPds	 	;pdsNummer++) { 
        if(file_in.equals("none"))
            myPDE.initialCondition(diePfeiler[pdsNummer].pds, icp, nicp); 
        else
            readin();
			}
   }           
//************************************************************************  
    public void readin() {
	
     for (int pdsNummer=0;pdsNummer<ZahlVonPds;pdsNummer++)  {
       double[] min;
       double[] max;
       
       double[] mult;
       double dummy;
       
       int i,j,k;             
       int in_width, in_height;

       min=new double[fields];
       max=new double[fields];       
       mult=new double[fields];
      try {
//      For local file
//      DataInputStream dis = new DataInputStream(
//                                     new FileInputStream(file_in));

//    For remote file
      URL documentBase=getCodeBase();
      URL inputFile = new URL(documentBase,file_in);
      URLConnection fc = inputFile.openConnection();
      DataInputStream dis = new DataInputStream(
                                     fc.getInputStream());                                     
      in_width=dis.readInt();
      in_height=dis.readInt();
      if(in_width==width & in_height==height) {
         for(k=0;k<fields;k++) {
            min[k]=dis.readDouble();
            max[k]=dis.readDouble();
            mult[k]=dis.readDouble();
            System.out.println("Field "+k+" min= "+min[k]+" max= "+max[k]+" mult= "+mult[k]);
         }
         for (i = 0; i < width+1; i ++) {
             for (j= 0; j< height+1; j++) {
                   for(k=0;k<fields;k++) {
//  If want more accurate write out and read in use writeDouble and then readDouble
//                      dummy= dis.readDouble();
                        dummy= (double)dis.readByte();
                        if(dummy<0) dummy=dummy+256;
                        diePfeiler[pdsNummer].pds[k][i][j]=min[k]+(dummy)/mult[k];
 
                   }                   
                }
            }
      }
      else
            System.out.println("In data file width = "+in_width+" height = "+in_height);
      dis.close();
      } catch (IOException e) {
              System.out.println("File IO error");
      }
      }                                           
    }    
    
      public static void main(String args[]) { 
	{
        // Erzeugung eines neuen Dialoges
        // JFrame meinJDialog = new  JFrame();
        JDialog meinJDialog = new  JDialog();
        meinJDialog.setTitle("astro");
        meinJDialog.setSize(900,600);
 
         Zellnetz panel = new  Zellnetz(false);
        // Hier setzen wir die Hintergrundfarbe unseres JPanels auf rot
        panel.setBackground(Color.red);
        // Hier fügen wir unserem Dialog unser JPanel hinzu
        meinJDialog.add(panel);
         Panel panel2 = new  Panel();
        // Hier setzen wir die Hintergrundfarbe unseres JPanels auf rot
        panel2.setBackground(Color.green);
        // Hier fügen wir unserem Dialog unser JPanel hinzu
        meinJDialog.add(panel);

		meinJDialog.setVisible(true); 
			 Button a=new  Button("  a	");		
			 Button b=new  Button("  b	");		
			panel.add( a); 		
		 	panel.init();
			panel2.add( b); 
			a.show();
			b.show(); 
			panel.show();
			panel2.show();
			panel.run();
		  meinJDialog.pack(); 
		  meinJDialog.show(); 



		  }





















 
    }
 
	 
/** 
* @author Michael Cross                     MODIFIED
*/
//*********************************************************************
  class znPlotChoiceControls extends Panel implements ActionListener{

/**
* vector of TextFields
*/
       public CheckboxGroup plotFFTCbg ;
       public Checkbox plotFFTYes;
       public Checkbox plotFFTNo ;
       public CheckboxGroup plotTypeCbg;
       public Checkbox[] plotType;
       public CheckboxGroup schwarzw;
       public Checkbox schwarzwJa;
       public Checkbox schwarzwNein;
	   public CheckboxGroup Darstellungsauswahl;
       public Checkbox Netz;
       public Checkbox NNcb;
       public Checkbox EBMcb;
       public Checkbox EBMundMBMcb;
       public Checkbox MBMcb;
       public JCheckBox Galileo_cb,tanul_cb,Reaktion_cb,Entspannung_cb,SCHWARZWPRUEFUNG_cb,uebereinstimmen;
       int nTypes;
       boolean spectral;
	   
           


//*********************************************************************
/** Default constructor
*/
//*********************************************************************
        
      public znPlotChoiceControls (int n, boolean inSpectral) { 
             int nGrid=0;
             nTypes=n;
             spectral=inSpectral;  
             setLayout(new GridLayout(4,4));//sonst die Parameter haben nicht genügend Raum.
             if(nTypes>1) {
	 			add(new Label("plot:"));
				plotTypeCbg=new CheckboxGroup();
				plotType=new Checkbox[nTypes];
				for(int i=0;i<nTypes;i++) {
					plotType[i]=new Checkbox(String.valueOf(i+1),plotTypeCbg,true);
					add(plotType[i]);
				 }   
                 plotType[0].setState(true);  
             }     
             if(spectral) {
                 plotFFTCbg=new CheckboxGroup();
                 plotFFTYes=new Checkbox(" Yes",plotFFTCbg,true);
                 plotFFTNo=new Checkbox(" No",plotFFTCbg,false);
                 add(new Label("FFT:"));
                 add(plotFFTYes);
                 add(plotFFTNo);
                 plotFFTNo.setState(true);
             }
             if(true) {
                 schwarzw=new CheckboxGroup();
                 schwarzwJa=new Checkbox("cbgrp 1",plotFFTCbg,true);
                 schwarzwNein=new Checkbox("cbgrp 2",plotFFTCbg,false);
                 add(new Label("Zellen:"));
                 add(schwarzwJa);
                 add(schwarzwNein);
                 schwarzwJa.setState(false);
             }
             if(true) {
                 Darstellungsauswahl=new CheckboxGroup();
				 NNcb=new Checkbox("NN",Darstellungsauswahl,false); 
                 EBMcb=new Checkbox("EBM",Darstellungsauswahl,true);
                 EBMundMBMcb=new Checkbox("E-u.MBM",Darstellungsauswahl,false); 
				 MBMcb=new Checkbox("MBM",Darstellungsauswahl,false); 
				 Galileo_cb=new JCheckBox("Galileo",nimmNN().Galileo);  
				 tanul_cb=new JCheckBox("tanulj",nimmNN().tanulj);  
				 Reaktion_cb=new JCheckBox("Rea.",nimmNN().Reaktion);  
				 Entspannung_cb=new JCheckBox("Entsp.g",nimmNN().Entspannung);  
				 SCHWARZWPRUEFUNG_cb=new JCheckBox("SCHWZ",nimmNN().SCHWARZWPRUEFUNG);
				 uebereinstimmen=new JCheckBox("Ueb.einst.",nimmNN().uebereinstimmen);  
                 add(NNcb); 
                 //add(EBMcb);
                 //add(EBMundMBMcb);
                 add(MBMcb); 
				 add(Galileo_cb);
				 add(tanul_cb);
				 add(Reaktion_cb);
				 add(Entspannung_cb); 
				 add(SCHWARZWPRUEFUNG_cb); 
				 add(uebereinstimmen); 
             }
                    
 
				Galileo_cb.addActionListener(this);
				tanul_cb.addActionListener(this);
				Reaktion_cb.addActionListener(this);
				Entspannung_cb.addActionListener(this); 
				SCHWARZWPRUEFUNG_cb.addActionListener(this); 
				uebereinstimmen.addActionListener(this); 
	}
      
      
      public int getPlotType() {
            if(nTypes>1)
               for(int i=0;i<nTypes;i++)
                    if(plotType[i].getState()) return i;
            return 0;
      }
      
      public boolean getPlotFFT() {
           if(spectral) return plotFFTYes.getState();
           else return false;
      }
      
      public void setPlotFFT(boolean yesNo) {
        if(spectral) plotFFTYes.setState(yesNo);
        return;
      }  
        
      
      public void setPlotType(int n) {
            if(n<nTypes)
                plotType[n].setState(true);
      }     
                      
            
      public Insets insets() {
         return new Insets(20,20,20,0);
   }

    public void actionPerformed(ActionEvent Veranstaltung) 
    {
	if(Veranstaltung.getSource()==Galileo_cb)
		nimmNN().Galileo=Galileo_cb.isSelected(); 
	else
	if(Veranstaltung.getSource()==tanul_cb) //tanul/önmûködô 
		{

		if(Zellnetz.this.runAnimation) 
			 synchronized(diePfeiler[0].pds)
				{ 
				nimmNN().tanulj=tanul_cb.isSelected(); 
				}
		}
	else
	if(Veranstaltung.getSource()==Entspannung_cb)
		nimmNN().Entspannung=Entspannung_cb.isSelected(); 
	else
	if(Veranstaltung.getSource()==Reaktion_cb)
		nimmNN().Reaktion=Reaktion_cb.isSelected(); 
	else
	if(Veranstaltung.getSource()==SCHWARZWPRUEFUNG_cb)
		{
		nimmNN().SCHWARZWPRUEFUNG=SCHWARZWPRUEFUNG_cb.isSelected();  
		if(SCHWARZWPRUEFUNG_cb.isSelected())
			if(Reaktion_cb.isSelected())
				Reaktion_cb.doClick();  
		}
	else
	if(Veranstaltung.getSource()==uebereinstimmen)
		{
		nimmNN().uebereinstimmen=uebereinstimmen.isSelected();
		}
	}	  
	
}
	
}

  