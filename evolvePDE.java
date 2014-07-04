/*
 * @(#)evolvePDE.java  5/7/99
 *
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 *   I MODIFIED ONLY THREE LINES (and "protected") OF THIS CLASS AND PACKED THE OTHERS FILES HERE
 *
 * Evolves coupled PDEs in 2 dimensions using finite difference or pseudo-spectral method
 * Compatible with Java 1.0.
*/
import java.applet.Applet;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.lang.InterruptedException;
import java.io.*;
import java.net.*; 


public class evolvePDE extends Applet implements Runnable {

      protected static final int CGL=0;
      protected static final int BRUSSELATOR=1;
      protected static final int SCHNACKENBERG=2;
      protected static final int SH=3;
      protected static final int BARKLEY=4;      
      protected static final int BAR=5;
      protected static final int LIFSHITZ=6;
      protected static final int SH2=7;
      protected static final int FHN=8;
           
      

    //Declare variables and classes
    ThreadGroup appletThreadGroup;
    Thread runner;

    plotControls plotControls;        // User interface
    Label minLabel, maxLabel;
    textControls parameterControls;
    private plotChoiceControls plotChoice;
    
    plotCanvas canvas;                // Plotting region
    PDE myPDE;                        // PDE evolver
    myPallette myPal;                 // Pallette for plotting
         
    double[][][] psi;                 // Fields for evolving
    double[][][] psir;                // Fields for plotting
    int[]   pixels;                   // Pixels fed to plotter
    MemoryImageSource source;
    Image image;      
    
    double vals[] = new double[1];    // Values read from plotControls
           
    // Parameters: most are reset from html file
    int width;                      // Dimensions of array for evolving - must be
    int height;                     // power of 2 for spectral code
    int plotwidth;                  // Dimensions of array for plotting - must equal
    int plotheight;                 // width, height for finite difference code.
    int scalex;                     // Ratio of plotwidth to width
    int scaley;                     // Ratio of plotheight to height
	static int scalexy;
    int plotType;                   // Type of plot
    int oldPlotType;                // To see if type of plot changed
    int function;                   // Function type
    int fields;                     // Number of fields
    int palletteType;               // Pallette used
            
    double xmin;                    // Size
    double xmax;
    double ymin;
    double ymax;
    double mesh;                    // Mesh spacing
    double minimum;                 // User set minimum for plotting if not scaleMinMax
    double maximum;                 // User set maximum for plotting if not scaleMinMax
     
    double[] parameters;            // parameters of PDE
    int nParameters;
    
    protected static double dt;                      // Time stepping
    static double t=0;
    double steps=1.;                // Steps between plotting
    
    double[][] icp;                 // Parameters for initial condition
    int nicp;                       // Number of initial condition parameters
        
    // User input flags
    boolean scalePlot;              // If true plot scales with window
    boolean showTime;               // If true time is displayed
    boolean scaleMinMax;            // If true plots are scaled to min and max values
    boolean showMinMax;             // If true min and max values are shown
    boolean choosePlots;            // If true user may choose plot type
    boolean plotFFT;                // If true plots FFT for spectral code

    // Internal flag
    boolean runOnce=false;          // Set true after initial conditions plotted

    String file_in,file_out;        // Files for data read in and write out (set to
                                    // "none" for no file access).

    // Initialization method for applet
    public void init() {
    
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
			// case UE:
                   // myPDE = new Uebereinstimmung();
                   // break;
  
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
        
        // Print out parameters to console
        System.out.println("dt ="+dt);
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
		plotControls.addRenderButton();
        add("South", plotControls);
        if(showMinMax) {
            Panel topPanel = new Panel();
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            minLabel = new Label("Min = -0.00000000",Label.LEFT);
            topPanel.add(minLabel);
            maxLabel = new Label("Max = 0.00000000",Label.LEFT);
            topPanel.add(maxLabel);
            add("North",topPanel);
        }

        Panel rightPanel = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH; 
        rightPanel.setLayout(gridbag);
        add("East",rightPanel);   

        String[] textboxes = new String[nParameters];
        for(int i=0;i<nParameters;i++)
                  textboxes[i]= String.valueOf(parameters[i]);        
        parameterControls = new textControls(textboxes,myPDE.labels,nParameters,5);

        constraints.gridwidth=1;
        constraints.weightx=1;
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        constraints.weighty=4;        
        gridbag.setConstraints(parameterControls, constraints);
        rightPanel.add(parameterControls);                            

        if(choosePlots) {
            plotChoice=new plotChoiceControls(myPDE.numPlotTypes,myPDE.spectral);
            constraints.weighty=1;
            gridbag.setConstraints(plotChoice, constraints);                      
            rightPanel.add(plotChoice);
            plotChoice.setPlotType(plotType);        
            if(myPDE.spectral) plotChoice.setPlotFFT(plotFFT);
        }
       
        add("Center", canvas = new plotCanvas());
        if(!scalePlot) canvas.setSize(plotwidth,plotheight);        
        
        // Set up solver, and plotting
        myPDE.init(width, height,parameters,dt,mesh,plotType);
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

//************************************************************************
    public void destroy() {
        remove(plotControls);
        remove(canvas);
//      System.exit(0);              // Exit for application
    }

//************************************************************************
    public void start() {
        if(plotControls.reset) {
            setInitialConditions();
            runOnce=false;
            plotControls.reset=false;
        }  
        runner = new Thread(this);
        runner.start();
    }

//************************************************************************
    public void stop() {
      runner = null;
      plotControls.runAnimation=false;
      plotControls.setButtonLabel(" Start ");
    }
    
//************************************************************************
    // Main method is only used for application not applet
    public static void main(String args[]) {
      Frame f = new Frame("evolvePDE");
      evolvePDE  evolve = new evolvePDE();
      evolve.init();

      f.add("Center", evolve);
      f.pack();
      f.show();

      evolve.start();
    }
    
//************************************************************************
    public void run() {
        while(plotControls.runAnimation) {
//            Don't seem to need next line 5/27/99
//            canvas.setImage(null);            // Wipe previous image
              Image img = calculateImage();
              synchronized(this) {
                  if (img != null && runner == Thread.currentThread())
                      canvas.setImage(img);
              }
        }
        plotControls.setStart();       // Set plotControls ready to start again
        if((!file_out.equals("none")) & runOnce) writeout();
    }
  
//************************************************************************
/**
* Calculates and returns the image.  Halts the calculation and returns
* null if the Applet is stopped during the calculation.
*/
//************************************************************************    
    Image calculateImage() {

      int i,j,k,ist,count;

      // Update parameters from GUI
      plotControls.getParams(vals);
      count=(int)vals[0];
      for(i=0;i<nParameters;i++)
               parameters[i]=parameterControls.parseTextField(i,parameters[i]);
      myPDE.setParameters(nParameters,parameters);

      // Evolve equations if not i.c., otherwise get ready
      if(runOnce) {
           ist=0;
           while(ist++<count && plotControls.runAnimation) 
                  t=myPDE.tstep(psi,t);
      }
                  
      for(k=0;k<fields;k++) {
          // Interpolate into larger plot range
          if(plotwidth!=width || plotheight!=height)
            for (j = 1; j <= plotheight; j++)
              for (i = 1; i <= plotwidth; i++)
                   psir[k][i][j]=0.;

          for (j = 1; j <= height; j++) 
              for (i = 1; i <= width; i++) 
                   psir[k][i][j]=psi[k][i][j];
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
      if(showTime) plotControls.setTime("Time = "+String.valueOf((float) t));
           
      // If this is intial condition get ready to animate
      if(!runOnce) {
         plotControls.runAnimation=false;
         plotControls.buttonEnable();
         runOnce=true;
      }      

      // Poll once per frame to see if we've been told to stop.
      Thread me = Thread.currentThread();            
      if (runner != me) return null;

      source.newPixels();      
      return image;

//    (Changed 5/5/99). Before, made new image each update.
//    Replaced by making image in initialization and only updating here.
//    return createImage(new MemoryImageSource(plotwidth, plotheight,
//                ColorModel.getRGBdefault(), pixels, 0, plotwidth));

    }

//************************************************************************
/**    
* Calculates pixel map from field 
* @param data array of real space data
* @param pixels array of packed pixels
* @param nx number of pixels in x-direction
* @param ny number of pixels in y-direction
*/
//************************************************************************
    protected void scale(double[][] data, int[] pixels, int nx, int ny) {
       double min;
       double max;
       double mult;
       int plotdata;
       int c[] = new int[4];
       int index = 0;
       int i,j;
       
       min=1000000.;
       max=-1000000.;
       if(scaleMinMax || showMinMax) {
           for(j=1;j<=ny;j++){
               for(i=1;i<=nx;i++) {
                   min = Math.min(min,data[i][j]);
                   max = Math.max(max,data[i][j]);
               }
           }
       }       
       if(showMinMax) {
           minLabel.setText("Min = "+String.valueOf((float) min));       
           maxLabel.setText("Max = "+String.valueOf((float) max)); 
       }
       if(!scaleMinMax) {
             min=minimum;
             max=maximum;
       }
                 
       mult=255./(max-min);
       
       for(j=1;j<=ny;j++) {
           for(i=1;i<=nx;i++){
                plotdata=(int)(mult*(data[i][j]-min));
                if(plotdata<0) plotdata=0;
                if(plotdata>255) plotdata=255;                
                c[0] = myPal.r[plotdata];
                c[1] = myPal.g[plotdata];
                c[2] = myPal.b[plotdata];
                c[3] = 255;
                pixels[index++] = ((c[3] << 24) |
                           (c[0] << 16) |
                           (c[1] << 8) |
                           (c[2] << 0));                
           }
       }       
    }
           
//************************************************************************       
/**
* Gets parameters from html file and sets default values
*/
//************************************************************************
    protected void getAllParameters() {
            width=(new Integer(getFromApplet("solvewidth","64"))).intValue();
            height=(new Integer(getFromApplet("solveheight","64"))).intValue();
            plotwidth=(new Integer(getFromApplet("plotwidth","64"))).intValue();            
            plotheight=(new Integer(getFromApplet("plotheight","64"))).intValue();
            choosePlots=false;
            if(getFromApplet("chooseplots","false").toLowerCase().equals("true"))
                  choosePlots=true;
            plotType=(new Integer(getFromApplet("plottype","0"))).intValue();
            plotFFT=false;
            if(getFromApplet("plotspectral","false").toLowerCase().equals("true"))
                  plotFFT=true;
            palletteType=(new Integer(getFromApplet("pallette","0"))).intValue();            
            steps=(new Double(getFromApplet("speed","1."))).doubleValue();            
            for(int i=0;i<nParameters;i++)
                 parameters[i]=(new Double(getFromApplet("parameter"+i,myPDE.defaultValues[i]))).doubleValue();
            dt=(new Double(getFromApplet("dt","0.1"))).doubleValue();
            mesh=(new Double(getFromApplet("mesh","1."))).doubleValue();
            scalePlot=false;
            if(getFromApplet("scaleplot","false").toLowerCase().equals("true"))
                  scalePlot=true;
            scaleMinMax=true;
            if(getFromApplet("scaleminmax","true").toLowerCase().equals("false"))
                  scaleMinMax=false;
            showMinMax=true;
            if(getFromApplet("showminmax","true").toLowerCase().equals("false"))
                  showMinMax=false;
            minimum=(new Double(getFromApplet("minimum","0."))).doubleValue();
            maximum=(new Double(getFromApplet("maximum","1."))).doubleValue();
            showTime=false;
            if(getFromApplet("showtime","false").toLowerCase().equals("true"))
                  showTime=true;            
            file_in=getFromApplet("filein","none").toLowerCase();
            System.out.println("Input_file "+file_in);
            file_out=getFromApplet("fileout","none").toLowerCase();

    }

//************************************************************************    
    protected String getFromApplet(String parameter, String s) {
        String getString = getParameter(parameter);
        if(getString == null) return s;
        else if(getString.length() == 0) return s;
        else return getString;
    } 
    
//************************************************************************    
    private void setInitialConditions() {
        t=0.;
        if(file_in.equals("none"))
            myPDE.initialCondition(psi, icp, nicp);
        else
            readin();
   }
//************************************************************************               
    public void readin() {
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
                        psi[k][i][j]=min[k]+(dummy)/mult[k];
 
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
    
    // Only works for applet started locally from directory in CLASSPATH
    public void writeout() {
      
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
      
      for(i=0;i<width+1;i++){
           for(j=0;j<height+1;j++) {
               for(k=0;k<fields;k++) {
                  min[k] = Math.min(min[k],psi[k][i][j]);
                  max[k] = Math.max(max[k],psi[k][i][j]);
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
       for(i=0;i<width+1;i++){
            for(j=0;j<height+1;j++) {
                for(k=0;k<fields;k++) {                       
//    If want more accurate write out and read in use writeDouble and then readDouble
//                  dos.writeDouble((mult[k]*(psi[k][i][j]-min[k])));
                    dos.writeByte((byte)Math.rint(mult[k]*(psi[k][i][j]-min[k])));
                }
            }
       }     
      dos.close();
      } catch (IOException e) {System.out.println("File IO error");
        }                                                  
    }
//************************************************************************
    public String getAppletInfo() {
        return "Evolution of 2d PDEs ";
    }

//************************************************************************   
    public String[][] getParameterInfo() {
       String[][] info = {
       // Parameter Name  Kind of Value   Description
         {"function",     "int",          "PDE used 0=CGL, 1=Brusselator, 2=Schnackenberg, 3=SH, 4=Barkley, 5=Bar, 6=Lifshitz"},
         {"solvewidth",   "double",       "Width of system (default=64)"},
         {"solveheight",  "double",       "Width of system (default=64)"},
         {"plotwidth",    "double",       "Scaled width for plotting (default=64)"},
         {"plotheight",   "double",       "Scaled height for plotting (default=64)"},
         {"parametersi",  "double",       "i=0,1... parameters of PDE (number and defaults depend on PDE"},
         {"ai",           "double",       "i=0,1.. amplitude of random i.c. for each field (default 0.5)"},
         {"bi",           "double",       "i=0,1.. offset of random i.c. for each field (default 0)"},
         {"dt",           "double",       "Time step (defulat=0.1)"},
         {"mesh",         "double",       "Size of spatial grid (default=1)"},
         {"speed",        "double",       "Number of iterations before plot (default 1)"},
         {"choosePlots",  "boolean",      "User may choose plot type if true"},
         {"plottype",     "int",          "Determines plot (depends on function) default=0)"},
         {"plotspectral", "boolean",      "Plot FFT for spectral code if true (default false)"},
         {"pallette",     "int",          "pallette used in plot 0=PDE default 1=RGB 2=GREY 3=CIRC 4=USER"},
         {"scaleplot",    "boolean",      "Plot scales with window if true (default false)"},
         {"scaleminmax",  "boolean",      "Automatically scale field in plot if true (default)"},
         {"showminmax",   "boolean",      "Show range of field in plot if true (defulat)"},
         {"minimum",      "double",       "Minimum value of field in plot if !scaleminmax (default 0)"},
         {"maximum",      "double",       "Maximum value of field in plot if !scaleminmax (default 1)"},
         {"showtime",     "boolean",      "Time shown in plot if true (default false)"},
         {"filein",       "String",       "File for read in. No read if none (default)"},
         {"fileout",      "String",       "File for write out. No write if none (default)"},
         {"ai",           "double",       "i=0,1.. amplitude of random i.c. for each field (default 0.5)"},
         {"bi",           "double",       "i=0,1.. offset of random i.c. for each field (default 0)"}
       };
       return info;
    }

  
	
	protected void set_plotControls(){
        plotControls = new plotControls(  this, steps, showTime); 
	}
	
	} 

// Buttons and text input
  class plotControls extends Panel{
    Button button,button1;
    TextField dtBox;
    TextField timeBox;
    Label timeLabel;
    Label nnLabel;
    public boolean runAnimation;
    public boolean reset=false;
    private boolean showTime;
    
    Applet applet; 
    static LayoutManager dcLayout = new FlowLayout(FlowLayout.LEFT, 5, 5);

    // Constructor
    protected   plotControls( Applet app, double p1, boolean inShowTime) {  
      applet = app;
      showTime=inShowTime;
      setLayout(dcLayout);
      add(new Label("Speed",Label.RIGHT));

      add(dtBox = new TextField(Integer.toString((int)p1), 3));
      add(button = new Button(" Start "));
      button.disable();
//    button.addActionListener(this);           // Java 1.1 event handler
      add(button1=new Button("Reset")); 
      button1.disable();  
//    button1.addActionListener(this);           // Java 1.1 event handler      
      if(showTime) {
            timeLabel = new Label("Time = 0     ",Label.LEFT);
            add(timeLabel);
      }      
      
            nnLabel = new Label("TEST = 0     ",Label.RIGHT);
            add(nnLabel);
			
			
      runAnimation=true;
    }
   
     
    public void addRenderButton() {
//      Moved these lines to constructor
//      add(button = new Button(" Start "));
//      button.disable();
//      add(button1=new Button("Reset"));
//      button1.disable();
//    button.addActionListener(this);           // Java 1.1 event handler
    }
          
      // Java 1.0 event handler
      public boolean action(Event evt, Object arg) {   
         if(evt.target==button) {
           if(runAnimation) {
                  button.disable();
                  runAnimation=false; 
//                Next two lines are for application, not applet
//                applet.stop();
//                applet.destroy();
             }
             else {
                   button.setLabel(" Stop  ");                  
                   runAnimation=true;
                   button1.disable();
                   applet.start();
             }                
             return true;
         }
         else if(evt.target==button1) {
            reset=true;
            if(!runAnimation) {
                button.disable();
                button.setLabel(" Start ");
                runAnimation=true;
                applet.start();
            }    
         return true;
         }       
        
        
		 
		 
	return false; 
}
	 


/*    Java 1.1 event handler 
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            if(runAnimation) {
                 button.setLabel("  Run ");
                 button1.enable();                 
                 runAnimation=false;
//               applet.stop();
//               applet.destroy();
            }
            else {
                  button.setLabel(" Stop  ");                  
                  runAnimation=true;
                  button1.disable();
                  applet.start();
            }      
        }
        else if (e.getSource() == button1) {
           reset=true;
            if(!runAnimation) {
                button.disable();
                button.setLabel(" Start ");
                runAnimation=true;
                applet.start();
            }          
        }
    }
*/    

 

    // Reads data from GUI and tests for validity
    public void getParams(double[] vals) {
      int iNew;
      double dNew;
      try {
          dNew=(new Double(dtBox.getText())).doubleValue();
      }
      catch (NumberFormatException e) {
          dtBox.setText(Integer.toString((int)vals[0]));
          return;
      }
      vals[0]=dNew;
//      try {
//          dNew=(new Double(parameterBox.getText())).doubleValue();
//      }
//      catch (NumberFormatException e) {
//          parameterBox.setText(String.valueOf(vals[1]));
//          return;
//      }
//      vals[1]=dNew;      
      return;
    }
    
    public void setButtonLabel(String text) {
        button.setLabel(text);
    }
    
    public void setTime(String text) {
        if(showTime) timeLabel.setText(text);
    }    
    public void setNn(String text) {
         nnLabel.setText(text);
    }    
    
    public void buttonEnable() {
        button.enable();
    }
    
    // Sets buttons ready to start
    public void setStart() {
        button.setLabel(" Start ");
        button1.enable();
        button.enable();
    }                

}

 
//*********************************************************************
/**
* Array of labelled text fields.<br>
* @version 11 November 1998
* @author Michael Cross
*/
//*********************************************************************
  class plotChoiceControls extends Panel {

/**
* vector of TextFields
*/
       public CheckboxGroup plotFFTCbg ;
       public Checkbox plotFFTYes;
       public Checkbox plotFFTNo ;
       public CheckboxGroup plotTypeCbg;
       public Checkbox[] plotType;
       int nTypes;
       boolean spectral;
           


//*********************************************************************
/** Default constructor
*/
//*********************************************************************
        
      public plotChoiceControls (int n, boolean inSpectral) {
             int nGrid=0;
             nTypes=n;
             spectral=inSpectral;
             if(nTypes>1) nGrid=nGrid+nTypes+1;
             if(spectral) nGrid=nGrid+3;    
             setLayout(new GridLayout(nGrid,1,0,0));
             if(nTypes>1) {
                 add(new Label("Plot type"));
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
                 add(new Label("Plot FFT"));
                 add(plotFFTYes);
                 add(plotFFTNo);
                 plotFFTNo.setState(true);
             }
                    
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


}
//*********************************************************************
//*********************************************************************


//*********************************************************************
/**
* Array of labelled text fields.<br>
* @version 11 November 1998
* @author Michael Cross
*/
//*********************************************************************
  class textControls extends Panel {

/**
* vector of TextFields
*/
        private TextField[] t;
        private Label[] l;
        private Panel[] p;
        private int ntext;           // number of text fields
        private int textLength;
//        private alertDialog alert;

//*********************************************************************
/** Default constructor
* @param text[] vector of text fields of TextBoxes
* @param l[] vector of text fields for labels
* @param n number of TextBoxes
* @param length length of TextBoxes
*/
//*********************************************************************
        
        public textControls (String[] text,
             String[] label, int n, int length) {
             textLength = length;  
             ntext = n;
             setLayout(new GridLayout(4,4,0,0));
             t = new TextField[ntext];
             l = new Label[ntext];
             p = new Panel[ntext];
                          
             for(int i=0;i<n;i++) {
                t[i] = new TextField(text[i],textLength);
                l[i] = new Label(label[i],Label.RIGHT);
                p[i] = new Panel();
                p[i].setLayout(new FlowLayout(FlowLayout.RIGHT));
               add(p[i]);
                p[i].add(l[i]);
                p[i].add(t[i]);
             }           
      }      

//**********************************************************************
/** Constructor with default TextBox length = 8
* @param text[] vector of text fields of TextBoxes
* @param l[] vector of text fields for labels
* @param n number of TextBoxes
*/
//**********************************************************************
      public textControls (String[] text, String[] l, int n) {
          this(text,l,n,8);
      }
      
//*********************************************************************
/**
* Number of controls
* @return number of controls
*/ 
//*********************************************************************
    
      public int ncontrols() {
           return ntext;
      }

//*********************************************************************
/**
* Parses text field known to be integer.  
* Resets old value of corresponding variable if input format
* is incorrect and brings up alertDialog warning box.
* @param n index of textbox to read
* @param i old value of variable
* @return new value of parameter if textbox format is correct,
* otherwise old value.
* @see alertDialog
*/
//*********************************************************************

    public int parseTextField(int n, int i) {
            int iNew;
            if(n>ntext) return i;
            try {
                iNew=(new Integer(t[n].getText())).intValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(i));
//                alert = new alertDialog(parent,"Try an integer");
                return i;
            }
            return iNew;
   }
//*********************************************************************
/**
* Parses text field known to be integer of known sign.  
* Resets old value of corresponding variable if input format
* is incorrect or wrong sign and brings up alertDialog warning box.
* @param n index of textbox to read
* @param i old value of variable
* @param positive true if value should be positive
* @return new value of parameter if textbox format is correct,
* and value of correct sign, otherwise old value.
* @see alertDialog
*/
//*********************************************************************

    public int parseTextField(int n, int i, boolean positive) {
            int iNew;
            if(n>ntext) return i;
            try {
                iNew=(new Integer(t[n].getText())).intValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(i));
//                alert = new alertDialog(parent,"Try an integer");
                return i;
            }
            if(((iNew < 0) && positive) || ((iNew>0) && !positive)) {
                t[n].setText(String.valueOf(i));
//                if(positive) alert = new alertDialog(parent,"Must be positive");
//                else alert = new alertDialog(parent,"Must be negative");
                return i;
            }
            return iNew;
   }
//*********************************************************************
/**
* Parses text field known to be integer in known range. 
* Resets old value of corresponding variable if input format
* is incorrect orout of range and brings up alertDialog warning box.
* @param n index of textbox to read
* @param i old value of variable
* @param min minimum value of allowed range
* @param max maximum value of allowed range
* @return new value of parameter if textbox format is correct,
* and value in of range, otherwise old value.
* @see alertDialog
*/
//*********************************************************************

    public int parseTextField(int n, int i, int min, int max) {
            int iNew;
            if(n>ntext) return i;
            try {
                iNew=(new Integer(t[n].getText())).intValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(i));
//                alert = new alertDialog(parent,"Try an integer");
                return i;
            }
            if((iNew < min) || (iNew > max)) {
                t[n].setText(String.valueOf(i));
//                alert = new alertDialog(parent,"Must be between " + min 
//                        +" and "+max);
                return i;
            }
            return iNew;
   }   
//*********************************************************************
/**
* Parses text field known to be double. 
* Resets old value of corresponding variable if input format
* is incorrect and brings up alertDialog warning box.
* @param n index of textbox to read
* @param d old value of variable
* @return new value of parameter if textbox format is correct,
* otherwise old value.
* @see alertDialog
*/
//*********************************************************************
   
    public double parseTextField(int n, double d) {
            double dNew;
            if(n>ntext) return d;
            try {
                dNew=(new Double(t[n].getText())).doubleValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                                           (parent,"Must be a number");
                return d;
            }
            return dNew;
   }

//*********************************************************************
/**
* Parses text field known to be double and of known sign 
* Resets old value of corresponding variable if input format
* is incorrect or wrong sign and brings up alertDialog warning box.
* @param n index of textbox to read
* @param d old value of variable
* @param positive true if positive, false if negative
* @return new value of parameter if textbox format is correct,
* and value in range, otherwise old value .
* @see alertDialog
*/
//*********************************************************************
   
    public double parseTextField (int n, double d, boolean positive) {
            double dNew;
            if(n>ntext) return d;
            try {
                dNew=(new Double(t[n].getText())).doubleValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                                           (parent,"Must be a number");
                return d;
            }
            if( (dNew <0 && positive) ) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                                        (parent,"Must be a positive number");
                return d;
            }
            else if( (dNew > 0 && !positive) ) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                                        (parent,"Must be a negative number");
                return d;
            }            
            return dNew;
   }

//*********************************************************************
/**
* Parses text field known to be double and in known range 
* Resets old value of corresponding variable if input format
* is incorrect or out of range and brings up alertDialog warning box.
* @param n index of textbox to read
* @param d old value of variable
* @param min minimum value of allowed range
* @param max maximum value of allowed range
* @return new value of parameter if textbox format is correct,
* and value in range, otherwise old value .
* @see alertDialog
*/
//*********************************************************************
   
    public double parseTextField (int n, double d, double min, double max) {
            double dNew;
            if(n>ntext) return d;
            try {
                dNew=(new Double(t[n].getText())).doubleValue();
            }
            catch (NumberFormatException e) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                                           (parent,"Must be a number");
                return d;
            }
            if( (dNew < min) || (dNew > max) ) {
                t[n].setText(String.valueOf(d));
//                alert = new alertDialog
//                     (parent,"Must be between " + min + " and " + max);
                return d;
            }
            return dNew;
   }

//*********************************************************************
/**
* Sets ith label
* @param i index of label
* @param text label
*/ 
//*********************************************************************   
   public void setLabelText(int i, String text) {
      l[i].setText(text);
   }

//*********************************************************************
/**
* Sets value of ith textbox
* @param i index of textbox
* @param text value to be set
*/ 
//*********************************************************************   
   public void setText(int i, String text) {
      t[i].setText(text);
   }
//*********************************************************************
/**
* Gets value of ith textbox
* @param i index of textbox
* @return content of textbox
*/ 
//*********************************************************************    
   public String getText(int i) {
      return t[i].getText();
   }   

//*********************************************************************
/**
* Hides ith label and texbox
* @param i index
*/ 
//*********************************************************************    
   public void hide(int i) {
      l[i].hide();
      t[i].hide();
   }

//*********************************************************************
/**
* Shows ith label and texbox
* @param i index
*/ 
//*********************************************************************    
   public void show(int i) {
      l[i].show();
      t[i].show();
   }   

//*********************************************************************
/**
* Shows all labels and texboxes
*/ 
//*********************************************************************    
   public void showAll() {
      for(int i=0;i<ntext;i++) {
            l[i].show();
            t[i].show();
      }      
   }

//*********************************************************************
/**
* Disables ith text box
* @param i index
*/
//********************************************************************* 
   public void disableText(int i) {
          t[i].disable();
   }

//*********************************************************************
/**
* Enables ith text box
* @param i index
*/
//*********************************************************************    
   public void enableText(int i) {
          t[i].enable();
   }   
      
   public Insets insets() {
       return new Insets(20,0,20,0);
   }
       
}
//*********************************************************************
//*********************************************************************


 

/*
 * @(#)Bar.java  1.5 3/4/99
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 * Time stepper for Bar equation
 */
 

  class Bar extends fdPDE {
      private double d1;
      private int i,j;
      private double one_m_dt,one_o_a,dt_o_eps,b_o_a;
      private double DELTA,h,D,b; 
      private double amp; 
      private double frequency=0.5;    
            
      // Consructor
      public Bar() {
            fields=2;
            nParameters=5;
            numPlotTypes=2;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.3";
            defaultValues[1]="0.01";
            defaultValues[2]="0.005";
            defaultValues[3]="0.001";
            defaultValues[4]="0.0";
            labels = new String[nParameters];
            labels[0]="  a ";
            labels[1]="  b ";                  
            labels[2]=" eta";
            labels[3]="delta";
            labels[4]=" amp";
      }     

      public double tstep(double[][][] psi, double t) {
     
      double u_th,temp;
      int ktmp;
      
      /* interchange k and kprm */
      ktmp = kprm;
      kprm = k;
      k = ktmp;
      
      /* main loop */
      for(i=1; i<=nx; i++) {
            for(j=1; j<=ny; j++) {
                 /* MCC Include abs */
                 if(Math.abs(psi[0][i][j]) < DELTA ) {
                        psi[0][i][j] = D * lap[k][i][j];
                        psi[1][i][j] = one_m_dt * psi[1][i][j];
                  }
                  /* MCC: Modify for psi[0] near 1 */
                  else if(Math.abs(1-psi[0][i][j]) < DELTA) {
                        psi[0][i][j] = 1. + D * lap[k][i][j];
                        psi[1][i][j] = one_m_dt * psi[1][i][j] + dt;
                  }      
                  else {
                        u_th = one_o_a * psi[1][i][j] + b_o_a;
                        if(psi[0][i][j]<=0.33333)
                              psi[1][i][j] = psi[1][i][j] + dt * (- psi[1][i][j]);
                        else if(psi[0][i][j]>=1)
                              psi[1][i][j] = psi[1][i][j] + dt * (1. - psi[1][i][j]);
                        else      
                              psi[1][i][j] = psi[1][i][j] + dt * 
                              (1. - 6.75*psi[0][i][j]*(psi[0][i][j] - 1.)*(psi[0][i][j] - 1.) - psi[1][i][j]);
            /*  explicit form for F */
            /*
                        psi[0][i][j] = psi[0][i][j] + dt_o_eps * psi[0][i][j] * 
                         (1.0 - psi[0][i][j]) * (psi[0][i][j] - u_th) + D * lap[k][i][j];
            */                  
            /*  implicit form for F */
            
                        if(psi[0][i][j] < u_th)
                              psi[0][i][j] = psi[0][i][j] / (1. - dt_o_eps *
                              (1.0 - psi[0][i][j]) * (psi[0][i][j] - u_th))+ D * lap[k][i][j];
                        else {
                              temp = dt_o_eps * psi[0][i][j] * (psi[0][i][j] - u_th);
                              psi[0][i][j] = (psi[0][i][j] + temp) / (1. + temp) +
                                    D * lap[k][i][j];
                  }      
            
                  lap[kprm][i][j]   = lap[kprm][i][j] - 4.*psi[0][i][j];
                  lap[kprm][i+1][j] = lap[kprm][i+1][j] + psi[0][i][j];
                  lap[kprm][i-1][j] = lap[kprm][i-1][j] + psi[0][i][j];
                  lap[kprm][i][j+1] = lap[kprm][i][j+1] + psi[0][i][j];
                  lap[kprm][i][j-1] = lap[kprm][i][j-1] + psi[0][i][j];
                  }
             lap[k][i][j] = 0.;
             }
      }
            //Drving term
      if(amp>0) psi[0][1][1]=amp*(1-Math.sin(frequency*t));
        
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

     
     public void initialCondition(double[][][] psi, double[] a, double[] b) {
     
        double x,y,r,theta;
        
        for(int j=1;j<=ny;j++) {
            for(int i=1;i<=nx;i++) {
                x=((double)(i-nx/2))*parameters[3];
                y=((double)(j-ny/2))*parameters[3];
                theta=Math.atan2(x,y);
                r=Math.sqrt(x*x+y*y);
//               psi[1][i][j]=0.5*parameters[0]-parameters[1]+a1*Math.random();                 
//               psi[0][i][j]=a2+a3*Math.random();

               psi[1][i][j]=b[1]*(0.5*parameters[0]-parameters[1])+a[0]*Math.sin(a[1]*r+theta);                 
               psi[0][i][j]=b[1]*0.5+b[0]*Math.cos(a[1]*r+theta);
                if(psi[0][i][j]<0.) psi[0][i][j]=-psi[0][i][j];
                while(psi[0][i][j]>1.) psi[0][i][j]=psi[0][i][j]-1.;
            }
        }      
     }
              
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            one_o_a=1./parameters[0];
            b=parameters[1];            
            b_o_a=b*one_o_a;
            dt_o_eps=dt/parameters[2];
            DELTA=parameters[3];
            /* MCC Changed 5. -> 20. */
            amp=parameters[4];           
            D=dt/(mesh*mesh);
            one_m_dt=1.-dt; 
     }
          
     // Forms plot from two fields depending on plotType
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {

          for(j=1;j<=plotheight;j++) {
             for(i=1;i<=plotwidth;i++) {
                if(plotType==1) 
                   psir[0][i][j] = psir[1][i][j];
             }
          }                  
     }
          
     public void setPallette() {      
                  myPal = new RGBPallette();
     }             
}



/*
 * @(#)Barkley.java  1.5 3/4/99
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 * Time stepper for Barkley equation
 */
 

  class Barkley extends fdPDE {
      private double d1;
      private int i,j;
      private double one_m_dt,one_o_a,dt_o_eps,b_o_a;
      private double DELTA,h,D,b; 
      private double amp; 
      private double frequency=0.5;
            
      // Consructor
      public Barkley() {
            fields=2;
            nParameters=5;
            numPlotTypes=2;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.3";
            defaultValues[1]="0.01";
            defaultValues[2]="0.005";
            defaultValues[3]="0.001";
            defaultValues[4]="0.0";
            labels = new String[nParameters];
            labels[0]="  a ";
            labels[1]="  b ";                  
            labels[2]=" eta";
            labels[3]="delta";
            labels[4]=" amp";
      }     

      public double tstep(double[][][] psi, double t) {
     
      double u_th,temp;
      int ktmp;
      
      /* interchange k and kprm */
      ktmp = kprm;
      kprm = k;
      k = ktmp;
      
      /* main loop */
      for(i=1; i<=nx; i++) {
            for(j=1; j<=ny; j++) {
                 /* MCC Include abs */
                 if(Math.abs(psi[0][i][j]) < DELTA ) {
                        psi[0][i][j] = D * lap[k][i][j];
                        psi[1][i][j] = one_m_dt * psi[1][i][j];
                  }
                  /* MCC: Modify for psi[0] near 1 */
                  else if(Math.abs(1-psi[0][i][j]) < DELTA) {
                        psi[0][i][j] = 1. + D * lap[k][i][j];
                        psi[1][i][j] = one_m_dt * psi[1][i][j] + dt;
                  }      
                  else {
                        u_th = one_o_a * psi[1][i][j] + b_o_a;
                        psi[1][i][j] = psi[1][i][j] + dt * (psi[0][i][j] - psi[1][i][j]);
            /*  explicit form for F */
            /*
                        psi[0][i][j] = psi[0][i][j] + dt_o_eps * psi[0][i][j] * 
                         (1.0 - psi[0][i][j]) * (psi[0][i][j] - u_th) + D * lap[k][i][j];
            */                  
            /*  implicit form for F */
            
                  if(psi[0][i][j] < u_th)
                        psi[0][i][j] = psi[0][i][j] / (1. - dt_o_eps *
                        (1.0 - psi[0][i][j]) * (psi[0][i][j] - u_th))+ D * lap[k][i][j];
                  else {
                        temp = dt_o_eps * psi[0][i][j] * (psi[0][i][j] - u_th);
                        psi[0][i][j] = (psi[0][i][j] + temp) / (1. + temp) +
                              D * lap[k][i][j];
                  }
                 
           
                  lap[kprm][i][j]   = lap[kprm][i][j] - 4.*psi[0][i][j];
                  lap[kprm][i+1][j] = lap[kprm][i+1][j] + psi[0][i][j];
                  lap[kprm][i-1][j] = lap[kprm][i-1][j] + psi[0][i][j];
                  lap[kprm][i][j+1] = lap[kprm][i][j+1] + psi[0][i][j];
                  lap[kprm][i][j-1] = lap[kprm][i][j-1] + psi[0][i][j];
                  }
             lap[k][i][j] = 0.;
             }
      }
      //Drving term
      if(amp>0) psi[0][1][1]=amp*(1-Math.sin(frequency*t));
/*    Use to make spiral (b=0.035 works well)
      if(kick) {
            psi[0][3*nx/4][ny/4]=5*amp;
            psi[0][3*nx/4+1][ny/4]=5*amp;
            psi[0][3*nx/4][ny/4+1]=5*amp;
            psi[0][3*nx/4-1][ny/4]=5*amp;
            psi[0][3*nx/4][ny/4-1]=5*amp;
            psi[0][3*nx/4+2][ny/4]=5*amp;
            psi[0][3*nx/4][ny/4+2]=5*amp;
            psi[0][3*nx/4-2][ny/4]=5*amp;
            psi[0][3*nx/4][ny/4-2]=5*amp;         
      }
*/      
        
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
     
     public void initialCondition(double[][][] psi, double[] a, double[] b) {
     
        double x,y,r,theta;
        
        for(int j=1;j<=ny;j++) {
            for(int i=1;i<=nx;i++) {
                x=((double)(i-nx/2))*parameters[3];
                y=((double)(j-ny/2))*parameters[3];
                theta=Math.atan2(x,y);
                r=Math.sqrt(x*x+y*y);
//               psi[1][i][j]=0.5*parameters[0]-parameters[1]+a1*Math.random();                 
//               psi[0][i][j]=a2+a3*Math.random();

               psi[1][i][j]=b[1]*(0.5*parameters[0]-parameters[1])+a[0]*Math.sin(a[1]*r+theta);                 
               psi[0][i][j]=b[1]*0.5+b[0]*Math.cos(a[1]*r+theta);
                if(psi[0][i][j]<0.) psi[0][i][j]=-psi[0][i][j];
                while(psi[0][i][j]>1.) psi[0][i][j]=psi[0][i][j]-1.;
            }
        }      
     }
              
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            one_o_a=1./parameters[0];
            b=parameters[1];            
            b_o_a=b*one_o_a;
            dt_o_eps=dt/parameters[2];
            DELTA=parameters[3];
            amp=parameters[4];           
            D=dt/(mesh*mesh);
            one_m_dt=1.-dt;             
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















 

  class plotCanvas extends Canvas {
  static int done ;
    Image img;
    boolean scalePlot=true;
    int plotwidth, plotheight,w,h; 
	
    public plotCanvas(){
		super();
		plotwidth=0;
		plotheight=0;
		done=0;
	}
	
    public void paint(Graphics g) {
	  if((getSize().height<1)||(getSize().width<1))
		return;
	  if((plotheight<1)||(plotwidth<1))
		return;
      if(scalePlot) { 
          w = getSize().width;
          h = getSize().height;
          if(false)//squared
			w=h= (w < h) ? w : h; 
      }
      else {
          w=plotwidth;
          h=plotheight;
      }        
 
	if(false)System.out.println("Painting on "+plotwidth+" x "+plotheight+" pixels.");
    if (img == null) {
//        throw new AssertionError("  img == null  ");
		//  super.paint(g);
      } else {              
			g.setColor(Color.red);  

			if(done<10){ g.drawRect(2, 2, w-4, h );done++;}
			g.drawImage(img, 10, 10, w-20, h , this);
			double B=(int)(double)topologischeBoltzmannmaschine.Beispiel().Breite();
			double scalex=evolvePDE.scalexy; 
			double scaley=evolvePDE.scalexy; 
			if(true){scalex=(int)(w/B);
			scaley=scalex;
			}
			System.out.println("scalexy="+scalex); 
			System.out.println("w="+w);
			System.out.println("h="+h);
			System.out.println("B="+B);
			System.out.println();
			g.setColor(Color.blue);  
			if(false)g.drawRect(10, 10,(int)(double)B,(int)(double)B ); 
			g.setColor(Color.green);   
			g.drawRect(10, 10,(int)(double)(B*scalex) ,(int)(double)(B*scaley) ); 
      }
    }

    public void update(Graphics g) {
      paint(g);
    }
    
    public void setSize(int width, int height) { //von java.awt.Container.doLayout angeruft 
		  if((height<1)||(width<1))// Es ist zu klein! 0 pixels.
						// at plotCanvas.setSize(evolvePDE.java:1665)
						// at java.awt.BorderLayout.layoutContainer(Unknown Source)
						// at java.awt.Container.layout(Unknown Source)
						// at java.awt.Container.doLayout(Unknown Source)
						// at java.awt.Container.validateTree(Unknown Source)
						// at java.awt.Container.validateTree(Unknown Source)
						// at java.awt.Container.validate(Unknown Source)
						// at sun.plugin.util.GrayBoxPainter.suspendPainting(Unknown Source)
						// at sun.plugin.AppletViewer.showAppletStatus(Unknown Source)
						// at sun.applet.AppletPanel.run(Unknown Source)
						// at java.lang.Thread.run(Unknown Source)
				{		if(false)System.out.println(width+" x "+height+" pixels!!!!");
				return;
				}

		  if(false) //Ende Mai : nur um BM zu überprüfen.
			if((height<210)||(width<210))
			throw new AssertionError(" Es ist zu klein! "+width+"  "+height+" pixels."); 
		  if(true)
			System.out.println(width+" x "+height+" pixels.");
		  if(!scalePlot)
			{
			System.out.println("Nicht geändert!");
			return;
			}
          plotwidth=width;
          plotheight=height; 
    }
  public void setSizeProgrammer(int width, int height) {    
		  setSize(width,height); 
          scalePlot=false;
    }

    // Next few methods are for interacting with GUI
    public Dimension getMinimumSize() {
    return new Dimension(200, 200);
  }

    public Dimension getPreferredSize() {
      return new Dimension(0600, 0600);//276);
     }

    public Image getImage() {
      return img;
    }

    public void setImage(Image img) {
      this.img = img;
        paint(getGraphics());
    }
}

/*
 * @(#)Brusselator.java  1.5 98/11/5
 *
 * Copyright (c) 1998, Michael Cross All Rights Reserved.
 *
 * Time stepper for Complex Ginzburg Landau Equation using pseudo-spectral method
 */ 

  class Brusselator extends spectralPDE {
      private double a,b,d1,d2;
      private int i,j;
      
      // Consructor
      public Brusselator() {
            spectral=false;
            numPlotTypes=2;
            nParameters=4;
            defaultValues=new String[nParameters];
            defaultValues[0]="2.";
            defaultValues[1]="5.";
            defaultValues[2]="1.";
            defaultValues[3]="4.";
            labels = new String[nParameters];
            labels[0]=" a ";   
            labels[1]=" b ";                  
            labels[2]=" d1";   
            labels[3]=" d2";
            fields=2;               
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
//             for(j=1;j<=plotheight;j++) {
//                  for(i=1;i<=plotwidth;i++) {
//                     psir[0][i][j] = psir[1][i][j];
//                  }
//             }
//          }                    
//     }
     
     
     public void setPallette() {      
                  myPal = new greyPallette();
     }
     
     protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=(b-1.0) - d1*k*k;
        d[1]= -a*a - d2*k*k;
        return d;
    }
    
     protected void formNonlinearTerm() {
         int i,j;
         double square;
         for(j=1;j<=ny;j++) {
            for(i=1;i<=nx;i++) {
                square = (b/a)*Math.pow(psip[0][i][j],2)+2.*a*psip[0][i][j]*psip[1][i][j]
                     + Math.pow(psip[0][i][j],2)*psip[1][i][j];
                psit[0][i][j] = square + a*a*psip[1][i][j]; 
                psit[1][i][j] = -square - b*psip[0][i][j];
            }
         }
     }                                           
}



/*
 * @(#)CCGL.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Time stepper for lattice of Complex Ginzburg Landau oscillators
 */
 

  class CCGL extends CODE {
      private double c3,D,square;
      private int i,j,k;     
      
      
      // Consructor
      public CCGL() {
      
            // Number of fields
            fields=2;
            numPlotTypes=2;
            spectral=false; 
            
            // Parameters of equation
            nParameters=2;
            defaultValues=new String[nParameters];
            defaultValues[0]="1.25";
            defaultValues[1]="1";
            labels = new String[nParameters];
            labels[0]=" c3 ";             
            labels[1]=" D ";
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            c3=parameters[0];
            D=parameters[1];
     }
         
     public void derivs(double[][][] psi, double[][][] dpsi, double t, int nx, int ny, int fields){

           for(i=1;i<=nx;i++) {
             for(j=1;j<=ny;j++) {        
               square = Math.pow(psi[0][i][j],2)+Math.pow(psi[1][i][j],2);
               dpsi[0][i][j] = (1.0-square)*psi[0][i][j]+(1.0-square)*c3*psi[1][i][j]
                            +D*(psi[0][i-1][j]+psi[0][i+1][j]+psi[0][i][j-1]+psi[0][i][j+1]
                                -4*psi[0][i][j]);
               dpsi[1][i][j] = (1.0-square)*psi[1][i][j]-(1.0-square)*c3*psi[0][i][j]
                            +D*(psi[1][i-1][j]+psi[1][i+1][j]+psi[1][i][j-1]+psi[1][i][j+1]
                                -4*psi[1][i][j]);               
               ;
          }
       }          
     
      }
    
          
     // Choose what to plot: return field to plot in psir[0]
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {

          int i,j,k;
           for(j=1;j<=plotheight;j++) {
             for(i=1;i<=plotwidth;i++) {
                if(plotType==0) 
                   psir[0][i][j] = Math.sqrt(Math.pow(psir[0][i][j],2)+Math.pow(psir[1][i][j],2));
                else  
                   psir[0][i][j] = Math.atan2(psir[1][i][j],psir[0][i][j]);
             }
          }                  
     }

     
     
     // Choose pallette for plot based on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette() {      
            if(plotType==0) 
                  myPal = new greyPallette();
            else
                  myPal = new circPallette();
     }
     
}

/*
 * @(#)CGL.java  1.5 98/11/5
 *
 * Copyright (c) 1998, Michael Cross All Rights Reserved.
 *
 * Time stepper for Complex Ginzburg Landau Equation using pseudo-spectral method
 */
 

  class CGL extends spectralPDE {
      private double d1;
      private int i,j,k;     
      
      
      // Consructor
      public CGL() {
      
            // Number of fields
            fields=2;
            numPlotTypes=2;
            spectral=false; 
            
            // Parameters of equation
            nParameters=1;
            defaultValues=new String[1];
            defaultValues[0]="1.25";
            labels = new String[1];
            labels[0]=" c3 ";             
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            d1=parameters[0];
     }
     
     // Construct multipliers of linear terms in transform equation at wavevector k
      protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=1-k*k;
        d[1]=1-k*k;
        return d;
    }
    
    // Form nonlinear term: take field psip[][][] and construct nonlinear terms psip[][][]
     protected void formNonlinearTerm() {
       int i,j;
       double square;
       
       for(j=1;j<=ny;j++) {
          for(i=1;i<=nx;i++) {
             square = Math.pow(psip[0][i][j],2)+Math.pow(psip[1][i][j],2);
             psit[0][i][j] = -square*psip[0][i][j]+(1.0-square)*d1*psip[1][i][j];
             psit[1][i][j] = -square*psip[1][i][j]-(1.0-square)*d1*psip[0][i][j];
          }
       }                    
    }        
          
     // Choose what to plot: return field to plot in psir[0]
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {

          int i,j,k;
          for(k=0;k<fields;k++) {
            // Zero out zone boundary values      
              for(i=1;i<=plotwidth;i++)
                    psir[k][i][2]=0.;

              for(j=1;j<=plotheight;j++)    
                    psir[k][2][j]=0.;

              // Transform to real space and form plot quantities
              myFFT.trbak(psir[k],plotwidth,plotheight);
          }
          for(j=1;j<=plotheight;j++) {
             for(i=1;i<=plotwidth;i++) {
                if(plotType==0) 
                   psir[0][i][j] = Math.sqrt(Math.pow(psir[0][i][j],2)+Math.pow(psir[1][i][j],2));
                else  
                   psir[0][i][j] = Math.atan2(psir[1][i][j],psir[0][i][j]);
             }
          }                  
     }

     
     
     // Choose pallette for plot based on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette() {      
            if(plotType==0) 
                  myPal = new greyPallette();
            else
                  myPal = new circPallette();
     }
     
}


 
/*
 * @(#CODE.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Time stepper for coupled ODEs 
 */
 

  class CODE extends Lattice {
      
      protected double[][][] k1;
      protected double[][][] k2;
      protected double[][][] k3;
      protected double[][][] k4;
      protected double[][][] k5;
      protected double[][][] k6;      
      protected double[][][] dpsi;
                  
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  int in_plotType) {
                  
            super.init(width, height, in_parameters, in_dt, in_plotType);
            
            dpsi = new double[fields][width+2][height+2];
            k1 = new double[fields][width+2][height+2];
            k2 = new double[fields][width+2][height+2];  
            k3 = new double[fields][width+2][height+2];                       
            k4 = new double[fields][width+2][height+2];                       
            k5 = new double[fields][width+2][height+2];                                            
            k6 = new double[fields][width+2][height+2];                                   
            
      }   

      // RK4 time stepper. 
      public double tstep(double[][][] psi, double t) {
                   
            derivs(psi,k1,t,nx,ny,fields);
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                  psip[i][j][k]=psi[i][j][k]+0.5*dt*k1[i][j][k];
            setBoundaryConditions(psip, nx, ny, fields);
            derivs(psip,k2,t+0.5*dt,nx,ny,fields);
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                  psip[i][j][k]=psi[i][j][k]+0.5*dt*k2[i][j][k];
            setBoundaryConditions(psip, nx, ny, fields);
            derivs(psip,k3,t+0.5*dt,nx,ny,fields);
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                  psip[i][j][k]=psi[i][j][k]+dt*k3[i][j][k];
            setBoundaryConditions(psip, nx, ny, fields);
            derivs(psip,k4,t+dt,nx,ny,fields);
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                  psi[i][j][k]=psi[i][j][k]+dt*(0.5*k1[i][j][k]+k2[i][j][k]+k3[i][j][k]+0.5*k4[i][j][k])/3.;
            
            // Make sure boundary conditions still hold
            setBoundaryConditions(psi, nx, ny, fields); 
                           
            t = t + dt;
            return t;
      }      
      
        public void derivs(double[][][] psi, double[][][] dpsi, double t, int nx, int ny, int fields){}        
              
}








/*
 * @(#)coupledODE.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Evolves coupled ODEs or Maps in 2 dimensions
 * Compatible with Java 1.0.
*/
 
 

  class coupledODE extends Applet implements Runnable {

      private static final int CCGL=0;                
      private static final int QUAD=1;
      private static final int RNL=2;
      private static final int PHASE=3;
      

    //Declare variables and classes
    ThreadGroup appletThreadGroup;
    Thread runner;

    plotControls plotControls;        // User interface
    Label minLabel, maxLabel;
    textControls parameterControls;
    plotChoiceControls plotChoice;
    
    plotCanvas canvas;                // Plotting region
    Lattice myCODE;                      // Coupled ODE evolver
    myPallette myPal;                 // Pallette for plotting
         
    double[][][] psi;                 // Fields for evolving
    double[][][] psir;                // Fields for plotting
    int[]   pixels;                   // Pixels fed to plotter
    MemoryImageSource source;
    Image image;      
    
    double vals[] = new double[1];    // Values read from plotControls
           
    // Parameters: most are reset from html file
    int width;                      // Dimensions of array for evolving - must be
    int height;                     // power of 2 for spectral code
    int plotwidth;                  // Dimensions of array for plotting - must equal
    int plotheight;                 // width, height for finite difference code.
    int scalex;                     // Ratio of plotwidth to width
    int scaley;                     // Ratio of plotheight to height
    int plotType;                   // Type of plot
    int oldPlotType;                // To see if type of plot changed
    int function;                   // Function type
    int fields;                     // Number of fields
    int palletteType;               // Pallette used
            
    double xmin;                    // Size
    double xmax;
    double ymin;
    double ymax;
    double minimum;                 // User set minimum for plotting if not scaleMinMax
    double maximum;                 // User set maximum for plotting if not scaleMinMax
     
    double[] parameters;            // parameters of ODE
    int nParameters;
    
    double dt;                      // Time stepping
    double t=0;
    double steps=1.;                // Steps between plotting
    
    double[][] icp;                 // Parameters for initial condition
    int nicp;                       // Number of initial condition parameters
        
    // User input flags
    boolean scalePlot;              // If true plot scales with window
    boolean showTime;               // If true time is displayed
    boolean scaleMinMax;            // If true plots are scaled to min and max values
    boolean showMinMax;             // If true min and max values are shown
    boolean choosePlots;            // If true user may choose plot type
    boolean plotFFT;                // If true plots FFT for spectral code

    // Internal flag
    boolean runOnce=false;          // Set true after initial conditions plotted

    String file_in,file_out;        // Files for data read in and write out (set to
                                    // "none" for no file access).

    // Initialization method for applet
    public void init() {
    
        // Initialize function
        function=(new Integer(getFromApplet("function","0"))).intValue();

        switch (function) {
             case CCGL:
                   myCODE = new CCGL();
                   break;     
             case QUAD:
                   myCODE = new QUAD();
                   break;  
             case RNL:
                   myCODE = new RNL();
                   break;    
             case PHASE:
                   myCODE = new Phase();
                   break;                                                                     
             default:
                   myCODE = new CCGL();
                   break;
        }
        
        // Get parameters from html file
        nParameters=myCODE.nParameters;
        System.out.println("NParameters = "+nParameters);
        nicp=myCODE.nInitialParameters;        
        parameters=new double[nParameters];
        getAllParameters();
        oldPlotType=plotType;
        
        xmin=0.;
        xmax=width;
        ymin=0.;
        ymax=height;
                
        // Set pltting scale factors
        if(plotwidth<width) plotwidth=width;
        if(plotheight<height) plotheight=height;
        scalex=plotwidth/width;
        scaley=plotheight/height;
        myCODE.setScales(scalex,scaley);
        
        // Print out parameters to console
        System.out.println("dt ="+dt);
        for(int i=0;i<nParameters;i++)
            System.out.println("parameter"+i+" = "+parameters[i]);
        System.out.println("width ="+width+" height ="+height);
        System.out.println("plotwidth ="+plotwidth+" plotheight ="+plotheight);
        System.out.println("xmin ="+xmin+" xmax ="+xmax+" ymin ="+ymin+" ymax ="+ymax+" ");
                
        // Set up arrays
        fields=myCODE.fields;
        System.out.println("Number of fields= "+fields);
        icp=new double[fields][nicp];        
        for(int i=0;i<fields;i++) {
           for(int j=0;j<nicp;j++) {
              icp[i][j]=(new Double(getFromApplet("ic"+i+j,"0."))).doubleValue();            
              System.out.println("icp["+i+j+"]: "+icp[i][j]);
           }   
        }    
        psi = new double[fields][width+2][height+2];
        psir = new double[fields][plotwidth+2][plotheight+2];
        
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

        plotControls = new plotControls( this, steps, showTime);
        plotControls.addRenderButton();
        add("South", plotControls);
        if(showMinMax) {
            Panel topPanel = new Panel();
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            minLabel = new Label("Min = -0.00000000",Label.LEFT);
            topPanel.add(minLabel);
            maxLabel = new Label("Max = 0.00000000",Label.LEFT);
            topPanel.add(maxLabel);
            add("North",topPanel);
        }

        Panel rightPanel = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH; 
        rightPanel.setLayout(gridbag);
        add("East",rightPanel);   

        String[] textboxes = new String[nParameters];
        for(int i=0;i<nParameters;i++)
                  textboxes[i]= String.valueOf(parameters[i]);        
        parameterControls = new textControls(textboxes,myCODE.labels,nParameters,5);

        constraints.gridwidth=1;
        constraints.weightx=1;
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        constraints.weighty=4;        
        gridbag.setConstraints(parameterControls, constraints);
        rightPanel.add(parameterControls);                            

        if(choosePlots) {
            plotChoice=new plotChoiceControls(myCODE.numPlotTypes,myCODE.spectral);
            constraints.weighty=1;
            gridbag.setConstraints(plotChoice, constraints);                      
            rightPanel.add(plotChoice);
            plotChoice.setPlotType(plotType);        
            if(myCODE.spectral) plotChoice.setPlotFFT(plotFFT);
        }
       
        add("Center", canvas = new plotCanvas());
        if(!scalePlot) canvas.setSize(plotwidth,plotheight);        
        
        // Set up solver, and plotting
        myCODE.init(width, height,parameters,dt,plotType);
        if(palletteType!=0) myCODE.setPallette(palletteType);
        myPal = myCODE.getPallette();

        
      plotControls.getParams(vals);
      for(int i=0;i<nParameters;i++)
               parameters[i]=parameterControls.parseTextField(i,parameters[i]);
      myCODE.setParameters(nParameters,parameters);        
       
        // Set up initial conditions
        setInitialConditions();
       
        // Start thread for evolving
        appletThreadGroup = Thread.currentThread().getThreadGroup();      
    }

//************************************************************************
    public void destroy() {
        remove(plotControls);
        remove(canvas);
//      System.exit(0);              // Exit for application
    }

//************************************************************************
    public void start() {
        if(plotControls.reset) {
            plotControls.getParams(vals);
                for(int i=0;i<nParameters;i++)
                    parameters[i]=parameterControls.parseTextField(i,parameters[i]);
            myCODE.setParameters(nParameters,parameters);          
            setInitialConditions();
            runOnce=false;
            plotControls.reset=false;
        }  
        runner = new Thread(this);
        runner.start();
    }

//************************************************************************
    public void stop() {
      runner = null;
      plotControls.runAnimation=false;
      plotControls.setButtonLabel(" Start ");
    }
    
//************************************************************************
    // Main method is only used for application not applet
    public static void main(String args[]) {
      Frame f = new Frame("coupledODE");
      coupledODE  evolve = new coupledODE();
      evolve.init();

      f.add("Center", evolve);
      f.pack();
      f.show();

      evolve.start();
    }
    
//************************************************************************
    public void run() {
        while(plotControls.runAnimation) {
//            Don't seem to need next line 5/27/99
//            canvas.setImage(null);            // Wipe previous image
              Image img = calculateImage();
              synchronized(this) {
                  if (img != null && runner == Thread.currentThread())
                      canvas.setImage(img);
              }
        }
        plotControls.setStart();       // Set plotControls ready to start again
        if((!file_out.equals("none")) & runOnce) writeout();
    }
  
//************************************************************************
/**
* Calculates and returns the image.  Halts the calculation and returns
* null if the Applet is stopped during the calculation.
*/
//************************************************************************    
    Image calculateImage() {//LOW

//  erforderlich?: synchronized(diePfeiler[0].pds){
      int i,j,k,ist,count;

      // Update parameters from GUI
      plotControls.getParams(vals);
      count=(int)vals[0];
      for(i=0;i<nParameters;i++)
               parameters[i]=parameterControls.parseTextField(i,parameters[i]);
      myCODE.setParameters(nParameters,parameters);

      // Evolve equations if not i.c., otherwise get ready
      if(runOnce) {
           ist=0;
           while(ist++<count && plotControls.runAnimation) 
                  t=myCODE.tstep(psi,t);
      }
                  
      for(k=0;k<fields;k++) {
          // Interpolate into larger plot range
          if(plotwidth!=width || plotheight!=height)
            for (j = 1; j <= plotheight; j++)
              for (i = 1; i <= plotwidth; i++)
                   psir[k][i][j]=0.;

          for (j = 1; j <= height; j++) 
              for (i = 1; i <= width; i++) 
                   psir[k][i][j]=psi[k][i][j];
      }
      
      // Type of plot depends on function and user choice
      if(choosePlots) {
        plotType=plotChoice.getPlotType();
        myCODE.setPlotType(plotType);
        if(plotType!=oldPlotType) {
            myCODE.setPallette();
            myPal = myCODE.getPallette();
            oldPlotType=plotType;
        }
        if(plotChoice.getPlotFFT())
            myCODE.makeFFTPlot(psir,plotwidth,plotheight);
        else
            myCODE.makePlot(psir,plotwidth,plotheight);
      }
      else
         myCODE.makePlot(psir,plotwidth,plotheight);
      
      // Scale onto color plot
      scale(psir[0],pixels,plotwidth,plotheight); 
      if(showTime) plotControls.setTime("Time = "+String.valueOf((float) t));
           
      // If this is intial condition get ready to animate
      if(!runOnce) {
         plotControls.runAnimation=false;
         plotControls.buttonEnable();
         runOnce=true;
      }      

      // Poll once per frame to see if we've been told to stop.
      Thread me = Thread.currentThread();            
      if (runner != me) return null;

      source.newPixels();      
      return image;

//    (Changed 5/5/99). Before, made new image each update.
//    Replaced by making image in initialization and only updating here.
//    return createImage(new MemoryImageSource(plotwidth, plotheight,
//                ColorModel.getRGBdefault(), pixels, 0, plotwidth));

    }

//************************************************************************
/**    
* Calculates pixel map from field 
* @param data array of real space data
* @param pixels array of packed pixels
* @param nx number of pixels in x-direction
* @param ny number of pixels in y-direction
*/
//************************************************************************
    private void scale(double[][] data, int[] pixels, int nx, int ny) {
       double min;
       double max;
       double mult;
       int plotdata;
       int c[] = new int[4];
       int index = 0;
       int i,j;
       
       min=1000000.;
       max=-1000000.;
       if(scaleMinMax || showMinMax) {
           for(j=1;j<=ny;j++){
               for(i=1;i<=nx;i++) {
                   min = Math.min(min,data[i][j]);
                   max = Math.max(max,data[i][j]);
               }
           }
       }       
       if(showMinMax) {
           minLabel.setText("Min = "+String.valueOf((float) min));       
           maxLabel.setText("Max = "+String.valueOf((float) max)); 
       }
       if(!scaleMinMax) {
             min=minimum;
             max=maximum;
       }
                 
       mult=255./(max-min);
       
       for(j=1;j<=ny;j++) {
           for(i=1;i<=nx;i++){
                plotdata=(int)(mult*(data[i][j]-min));
                if(plotdata<0) plotdata=0;
                if(plotdata>255) plotdata=255;                
                c[0] = myPal.r[plotdata];
                c[1] = myPal.g[plotdata];
                c[2] = myPal.b[plotdata];
                c[3] = 255;
                pixels[index++] = ((c[3] << 24) |
                           (c[0] << 16) |
                           (c[1] << 8) |
                           (c[2] << 0));                
           }
       }       
    }
           
//************************************************************************       
/**
* Gets parameters from html file and sets default values
*/
//************************************************************************
    protected void getAllParameters() {
            width=(new Integer(getFromApplet("solvewidth","64"))).intValue();
            height=(new Integer(getFromApplet("solveheight","64"))).intValue();
            plotwidth=(new Integer(getFromApplet("plotwidth","64"))).intValue();            
            plotheight=(new Integer(getFromApplet("plotheight","64"))).intValue();
            choosePlots=false;
            if(getFromApplet("chooseplots","false").toLowerCase().equals("true"))
                  choosePlots=true;
            plotType=(new Integer(getFromApplet("plottype","0"))).intValue();
            plotFFT=false;
            if(getFromApplet("plotspectral","false").toLowerCase().equals("true"))
                  plotFFT=true;
            palletteType=(new Integer(getFromApplet("pallette","0"))).intValue();            
            steps=(new Double(getFromApplet("speed","1."))).doubleValue();            
            for(int i=0;i<nParameters;i++)

                 parameters[i]=(new Double(getFromApplet("parameter"+i,myCODE.defaultValues[i]))).doubleValue();
            dt=(new Double(getFromApplet("dt","0.1"))).doubleValue();

            scalePlot=false;
            if(getFromApplet("scaleplot","false").toLowerCase().equals("true"))
                  scalePlot=true;
				    
            scaleMinMax=true;
            if(getFromApplet("scaleminmax","true").toLowerCase().equals("false"))
                  scaleMinMax=false;
            showMinMax=true;
            if(getFromApplet("showminmax","true").toLowerCase().equals("false"))
                  showMinMax=false;
            minimum=(new Double(getFromApplet("minimum","0."))).doubleValue();
            maximum=(new Double(getFromApplet("maximum","1."))).doubleValue();
            showTime=false;
            if(getFromApplet("showtime","false").toLowerCase().equals("true"))
                  showTime=true;            
            file_in=getFromApplet("filein","none").toLowerCase();
            System.out.println("Input_file "+file_in);
            file_out=getFromApplet("fileout","none").toLowerCase();

    }

//************************************************************************    
    protected String getFromApplet(String parameter, String s) {
        String getString = getParameter(parameter);
        if(getString == null) return s;
        else if(getString.length() == 0) return s;
        else return getString;
    } 
    
//************************************************************************    
    private void setInitialConditions() {
        t=0.;
        if(file_in.equals("none"))
            myCODE.initialCondition(psi, icp, nicp);
        else
            readin();
   }
//************************************************************************               
    public void readin() {
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
                        psi[k][i][j]=min[k]+(dummy)/mult[k];
 
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
    
    // Only works for applet started locally from directory in CLASSPATH
    public void writeout() {
      
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
      
      for(i=0;i<width+1;i++){
           for(j=0;j<height+1;j++) {
               for(k=0;k<fields;k++) {
                  min[k] = Math.min(min[k],psi[k][i][j]);
                  max[k] = Math.max(max[k],psi[k][i][j]);
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
       for(i=0;i<width+1;i++){
            for(j=0;j<height+1;j++) {
                for(k=0;k<fields;k++) {                       
//    If want more accurate write out and read in use writeDouble and then readDouble
//                  dos.writeDouble((mult[k]*(psi[k][i][j]-min[k])));
                    dos.writeByte((byte)Math.rint(mult[k]*(psi[k][i][j]-min[k])));
                }
            }
       }     
      dos.close();
      } catch (IOException e) {System.out.println("File IO error");
        }                                                  
    }
//************************************************************************
    public String getAppletInfo() {

        return "Evolution of 2d PDEcoupled ODEs ";
    }

//************************************************************************   
    public String[][] getParameterInfo() {
       String[][] info = {
       // Parameter Name  Kind of Value   Description

         {"function",     "int",          "ODE used 0=CGL"},
         {"solvewidth",   "double",       "Width of system (default=64)"},
         {"solveheight",  "double",       "Width of system (default=64)"},
         {"plotwidth",    "double",       "Scaled width for plotting (default=64)"},
         {"plotheight",   "double",       "Scaled height for plotting (default=64)"},

         {"parametersi",  "double",       "i=0,1... parameters of CODE (number and defaults depend on CODE"},
         {"ai",           "double",       "i=0,1.. amplitude of random i.c. for each field (default 0.5)"},
         {"bi",           "double",       "i=0,1.. offset of random i.c. for each field (default 0)"},
         {"dt",           "double",       "Time step (defulat=0.1)"},

         {"speed",        "double",       "Number of iterations before plot (default 1)"},
         {"choosePlots",  "boolean",      "User may choose plot type if true"},
         {"plottype",     "int",          "Determines plot (depends on function) default=0)"},
         {"plotspectral", "boolean",      "Plot FFT for spectral code if true (default false)"},

         {"pallette",     "int",          "pallette used in plot 0=CODE default 1=RGB 2=GREY 3=CIRC 4=USER"},
         {"scaleplot",    "boolean",      "Plot scales with window if true (default false)"},
         {"scaleminmax",  "boolean",      "Automatically scale field in plot if true (default)"},
         {"showminmax",   "boolean",      "Show range of field in plot if true (defulat)"},
         {"minimum",      "double",       "Minimum value of field in plot if !scaleminmax (default 0)"},
         {"maximum",      "double",       "Maximum value of field in plot if !scaleminmax (default 1)"},
         {"showtime",     "boolean",      "Time shown in plot if true (default false)"},
         {"filein",       "String",       "File for read in. No read if none (default)"},
         {"fileout",      "String",       "File for write out. No write if none (default)"},
         {"ai",           "double",       "i=0,1.. amplitude of random i.c. for each field (default 0.5)"},
         {"bi",           "double",       "i=0,1.. offset of random i.c. for each field (default 0)"}
       };
       return info;
    }
}

/*
 * @(#)DitherTest.java    1.5 98/03/23
 * 
 */
 

  class DitherTest extends Applet implements Runnable {
    final static int NOOP = 0;
    final static int RED = 1;
    final static int GREEN = 2;
    final static int BLUE = 3;
    final static int ALPHA = 4;
    final static int SATURATION = 5;

    ThreadGroup appletThreadGroup;
    Thread runner;

    DitherControls XControls;
    DitherControls YControls;
    DitherCanvas canvas;

    public void init() {
    String xspec, yspec;
    int xvals[] = new int[2];
    int yvals[] = new int[2];

    try {
        xspec = getParameter("xaxis");
    } catch (Exception e) {
        xspec = null;
    }
    try {
        yspec = getParameter("yaxis");
	} catch (Exception e) {
	    yspec = null;
	}
	if (xspec == null) xspec = "red";
	if (yspec == null) yspec = "blue";
	int xmethod = colormethod(xspec, xvals);
	int ymethod = colormethod(yspec, yvals);

	setLayout(new BorderLayout());
	XControls = new DitherControls(this, xvals[0], xvals[1],
				       xmethod, false);
	YControls = new DitherControls(this, yvals[0], yvals[1],
				       ymethod, true);
	YControls.addRenderButton();
	add("North", XControls);
	add("South", YControls);
	add("Center", canvas = new DitherCanvas());

	appletThreadGroup = Thread.currentThread().getThreadGroup();
    }

    public void destroy() {
        remove(XControls);
        remove(YControls);
        remove(canvas);
    }

    public void start() {
        runner = new Thread(this);
	runner.start();
    }

    public void stop() {
	runner = null;
    }

    public static void main(String args[]) {
	Frame f = new Frame("DitherTest");
	DitherTest	ditherTest = new DitherTest();

	ditherTest.init();

	f.add("Center", ditherTest);
	f.pack();
	f.show();

	ditherTest.start();
    }

    int colormethod(String s, int vals[]) {
	int method = NOOP;

	if (s == null)
	    s = "";

	String lower = s.toLowerCase();
	int len = 0;
	if (lower.startsWith("red")) {
	    method = RED;
	    lower = lower.substring(3);
	} else if (lower.startsWith("green")) {
	    method = GREEN;
	    lower = lower.substring(5);
	} else if (lower.startsWith("blue")) {
	    method = BLUE;
	    lower = lower.substring(4);
	} else if (lower.startsWith("alpha")) {
	    method = ALPHA;
	    lower = lower.substring(4);
	} else if (lower.startsWith("saturation")) {
	    method = SATURATION;
	    lower = lower.substring(10);
	}

	if (method == NOOP) {
	    vals[0] = 0;
	    vals[1] = 0;
	    return method;
	}

	int begval = 0;
	int endval = 255;

	try {
	    int dash = lower.indexOf('-');
	    if (dash < 0) {
		begval = endval = Integer.parseInt(lower);
	    } else {
		begval = Integer.parseInt(lower.substring(0, dash));
		endval = Integer.parseInt(lower.substring(dash+1));
	    }
	} catch (Exception e) {
	}

	if (begval < 0) begval = 0;
	if (endval < 0) endval = 0;
	if (begval > 255) begval = 255;
	if (endval > 255) endval = 255;

	vals[0] = begval;
	vals[1] = endval;

	return method;
    }

    void applymethod(int c[], int method, int step, int total, int vals[]) {
	if (method == NOOP)
	    return;
	int val = ((total < 2)
		   ? vals[0]
		   : vals[0] + ((vals[1] - vals[0]) * step / (total - 1)));
	switch (method) {
	case RED:
	    c[0] = val;
	    break;
	case GREEN:
	    c[1] = val;
	    break;
	case BLUE:
	    c[2] = val;
	    break;
	case ALPHA:
	    c[3] = val;
	    break;
	case SATURATION:
	    int max = Math.max(Math.max(c[0], c[1]), c[2]);
	    int min = max * (255 - val) / 255;
	    if (c[0] == 0) c[0] = min;
	    if (c[1] == 0) c[1] = min;
	    if (c[2] == 0) c[2] = min;
	    break;
	}
    }

    public void run() {
        canvas.setImage(null);	// Wipe previous image
        Image img = calculateImage();
        synchronized(this) {
            if (img != null && runner == Thread.currentThread())
                canvas.setImage(img);
        }
    }
  
    /**
     * Calculates and returns the image.  Halts the calculation and returns
     * null if the Applet is stopped during the calculation.
     */
    Image calculateImage() {  //LOW
        Thread me = Thread.currentThread();

        int width = canvas.getSize().width;
	int height = canvas.getSize().height;
	int xvals[] = new int[2];
	int yvals[] = new int[2];
	int xmethod = XControls.getParams(xvals);
	int ymethod = YControls.getParams(yvals);
	int pixels[] = new int[width * height];
	int c[] = new int[4];
	int index = 0;
	for (int j = 0; j < height; j++) {
	    for (int i = 0; i < width; i++) {
		c[0] = c[1] = c[2] = 0;
		c[3] = 255;
		if (xmethod < ymethod) {
		    applymethod(c, xmethod, i, width, xvals);
		    applymethod(c, ymethod, j, height, yvals);
		} else {
		    applymethod(c, ymethod, j, height, yvals);
		    applymethod(c, xmethod, i, width, xvals);
		}
		pixels[index++] = ((c[3] << 24) |
				   (c[0] << 16) |
				   (c[1] << 8) |
				   (c[2] << 0));
	    }

            // Poll once per row to see if we've been told to stop.
            if (runner != me)
                return null;
	}

        return createImage(new MemoryImageSource(width, height,
		      ColorModel.getRGBdefault(), pixels, 0, width));
    }

    public String getAppletInfo() {
        return "An interactive demonstration of dithering.";
    }
  
    public String[][] getParameterInfo() {
        String[][] info = {
            {"xaxis", "{RED, GREEN, BLUE, PINK, ORANGE, MAGENTA, CYAN, WHITE, YELLOW, GRAY, DARKGRAY}", "The color of the Y axis.  Default is RED."}, 
            {"yaxis", "{RED, GREEN, BLUE, PINK, ORANGE, MAGENTA, CYAN, WHITE, YELLOW, GRAY, DARKGRAY}", "The color of the X axis.  Default is BLUE."}
        };
        return info;
    }
}

class DitherCanvas extends Canvas {
    Image img;
    static String calcString = "Calculating...";

    public void paint(Graphics g) {
	int w = getSize().width;
	int h = getSize().height;
	if (img == null) {
	    super.paint(g);
	    g.setColor(Color.black);
	    FontMetrics fm = g.getFontMetrics();
	    int x = (w - fm.stringWidth(calcString))/2;
	    int y = h/2;
	    g.drawString(calcString, x, y);
	} else {
            g.drawImage(img, 0, 0, w, h, this);
        }
    }

    public void update(Graphics g) {
	paint(g);
    }

    public Dimension getMinimumSize() {

    	return new Dimension(20, 20);
   
   }

   
   
   public Dimension getPreferredSize() {
	
	return new Dimension(200, 200);
    }

    public Image getImage() {
	return img;
    }

    public void setImage(Image img) {
	this.img = img;
        paint(getGraphics());
    }
}

class DitherControls extends Panel implements ActionListener {
    TextField start;
    TextField end;
    Button button;
    Choice choice;
    DitherTest applet;

    static LayoutManager dcLayout = new FlowLayout(FlowLayout.CENTER, 10, 5);

    public DitherControls(DitherTest app, int s, int e, int type,
			  boolean vertical) {
	applet = app;
	setLayout(dcLayout);
	add(new Label(vertical ? "Vertical" : "Horizontal"));
	add(choice = new Choice());
	choice.addItem("Noop");
	choice.addItem("Red");
	choice.addItem("Green");
	choice.addItem("Blue");
	choice.addItem("Alpha");
	choice.addItem("Saturation");
	choice.select(type);
	add(start = new TextField(Integer.toString(s), 4));
	add(end = new TextField(Integer.toString(e), 4));
    }

    public void addRenderButton() {
	add(button = new Button("New Image"));
	button.addActionListener(this);
    }

    public int getParams(int vals[]) {
	vals[0] = Integer.parseInt(start.getText());
	vals[1] = Integer.parseInt(end.getText());
	return choice.getSelectedIndex();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            applet.start();
        }
    }
}



 
  
/**                    
* Calculates 2d FFT
* Uses routines from Numerical Recipes 
* @version 1 November 1998
* @author Michael Cross
*/ 

class FFT2D {
 
      public void trfor(double[][] data, int nx, int ny) {
             realftx(data,nx,ny,1);
             realfty(data,nx,ny,1);
       
             double rnorm = 4./(double)(nx*ny);
             for(int j = 1;j<=ny;j++) {
                for(int i = 1;i<=nx;i++) {
                   data[i][j] = data[i][j]*rnorm;
                }
             }
      } 
      
      public void trbak(double[][] data, int nx, int ny) {
             realftx(data,nx,ny,-1);
             realfty(data,nx,ny,-1);
       
      }             
      
//**********************************************************************      
/**
* FFT from Numerical recipes modified to do x transform for 2d FFT.
* @param data array of nx x ny data points<br>
* On return contains transform <br>
* For packing see Numerical Recipes
* @param    nx number of data points in x direction
* @param    ny number of data points in y direction
* @param    isign 1 for forward; -1 for inverse
*/          
//**********************************************************************          
      
      private void four1x(double data[][], int nx, int ny, int isign) {
          int mmax,m,j,istep,i,k;
          double wtemp,wr,wpr,wpi,wi,theta;
          double tempr,tempi;
          double swap;
    
          j=1;
          for (i=1;i<nx;i+=2) {
                if (j > i) {
                   for(k=1;k<=ny;k++) {
                      swap=data[j][k];
                      data[j][k]=data[i][k];
                      data[i][k]=swap;
                      
                      swap=data[j+1][k];
                      data[j+1][k]=data[i+1][k];
                      data[i+1][k]=swap;
                   }                  
                }
                m=nx >> 1;
                while (m >= 2 && j > m) {
                      j -= m;
                      m >>= 1;
                }
                j += m;
          }
          mmax=2;
          while (nx > mmax) {
                istep=2*mmax;
                theta=isign*(6.28318530717959/mmax);
                wtemp=Math.sin(0.5*theta);
                wpr = -2.0*wtemp*wtemp;
                wpi=Math.sin(theta);
                wr=1.0;
                wi=0.0;
                for (m=1;m<mmax;m+=2) {
                      for (i=m;i<=nx;i+=istep) {
                            j=i+mmax;
                            for (k=1;k<=ny;k++) {
                                  tempr=wr*data[j][k]-wi*data[j+1][k];
                                  tempi=wr*data[j+1][k]+wi*data[j][k];
                                  data[j][k]=data[i][k]-tempr;
                                  data[j+1][k]=data[i+1][k]-tempi;
                                  data[i][k] += tempr;
                                  data[i+1][k] += tempi;
                            }     
                      }
                      wr=(wtemp=wr)*wpr-wi*wpi+wr;
                      wi=wi*wpr+wtemp*wpi+wi;
                }
                mmax=istep;
          }
     }

//**********************************************************************      
//**********************************************************************      
/**
* FFT from Numerical recipes modified to do y transform for 2d FFT
* @param data array of nx x ny data points<br>
* On return contains transform <br>
* For packing see Numerical Recipes
* @param    nx number of data poitns in x direction
* @param    ny number of data points in y direction
* @param    isign 1 for forward; -1 for inverse
*/          
//**********************************************************************          
      private void four1y(double data[][], int nx, int ny, int isign) {
          int mmax,m,j,istep,i,k;
          double wtemp,wr,wpr,wpi,wi,theta;
          double tempr,tempi;
          double swap;

          j=1;
          for (i=1;i<ny;i+=2) {
                if (j > i) {
                       for (k=1;k<=nx;k++) {
                          swap=data[k][j];
                          data[k][j]=data[k][i];
                          data[k][i]=swap;
                     
                          swap=data[k][j+1];
                          data[k][j+1]=data[k][i+1];
                          data[k][i+1]=swap;                  
                       }
                }
                m=ny >> 1;
                while (m >= 2 && j > m) {
                      j -= m;
                      m >>= 1;
                }
                j += m;
          }
          mmax=2;
          while (ny > mmax) {
                istep=2*mmax;
                theta=isign*(6.28318530717959/mmax);
                wtemp=Math.sin(0.5*theta);
                wpr = -2.0*wtemp*wtemp;
                wpi=Math.sin(theta);
                wr=1.0;
                wi=0.0;
                for (m=1;m<mmax;m+=2) {
                      for (i=m;i<=ny;i+=istep) {
                            j=i+mmax;
                            for (k=1;k<=nx;k++) {
                              tempr=wr*data[k][j]-wi*data[k][j+1];
                              tempi=wr*data[k][j+1]+wi*data[k][j];
                              data[k][j]=data[k][i]-tempr;
                              data[k][j+1]=data[k][i+1]-tempi;
                              data[k][i] += tempr;
                              data[k][i+1] += tempi;
                            }
                      }
                      wr=(wtemp=wr)*wpr-wi*wpi+wr;
                      wi=wi*wpr+wtemp*wpi+wi;
                }
                mmax=istep;
          }
     }
     
//**********************************************************************      
/**
* FFT from Numerical recipes
* @param data array of n x ny data points<br>
* On return contains transform <br>
* For packing see Numerical Recipes.
* Note: convention for nx is as in Numerical Recipes, not as in realftx.for
* @param    nx number of data points in x direction
* @param    ny number of data points in y direction
* @param    isign 1 for forward; -1 for inverse
*/          
//**********************************************************************          
     
      private void realftx(double data[][], int nx, int ny, int isign)   {
      
      int i,i1,i2,i3,i4,np3,k;
      double c1=0.5,c2,h1r,h1i,h2r,h2i;
      double wr,wi,wpr,wpi,wtemp,theta;

      theta=3.141592653589793/(double) (nx>>1);
      if (isign == 1) {
            c2 = -0.5;
            four1x(data,nx,ny,1);
      } else {
            c2=0.5;
            theta = -theta;
      }
      wtemp=Math.sin(0.5*theta);
      wpr = -2.0*wtemp*wtemp;
      wpi=Math.sin(theta);
      wr=1.0+wpr;
      wi=wpi;
      np3=nx+3;
      for (i=2;i<=(nx>>2);i++) {
            i4=1+(i3=np3-(i2=1+(i1=i+i-1)));
            for(k=1;k<=ny;k++) {
                  h1r=c1*(data[i1][k]+data[i3][k]);
                  h1i=c1*(data[i2][k]-data[i4][k]);
                  h2r = -c2*(data[i2][k]+data[i4][k]);
                  h2i=c2*(data[i1][k]-data[i3][k]);
                  data[i1][k]=h1r+wr*h2r-wi*h2i;
                  data[i2][k]=h1i+wr*h2i+wi*h2r;
                  data[i3][k]=h1r-wr*h2r+wi*h2i;
                  data[i4][k] = -h1i+wr*h2i+wi*h2r;
            }      
            wr=(wtemp=wr)*wpr-wi*wpi+wr;
            wi=wi*wpr+wtemp*wpi+wi;
      }
      if (isign == 1) {
            for(k=1;k<=ny;k++) {
                  data[1][k] = (h1r=data[1][k])+data[2][k];
                  data[2][k] = h1r-data[2][k];
            }
      } else {
            for(k=1;k<=ny;k++) {
                  data[1][k]=c1*((h1r=data[1][k])+data[2][k]);
                  data[2][k]=c1*(h1r-data[2][k]);
            }
            four1x(data,nx,ny,-1);
      }
 }
//**********************************************************************      
/**
* FFT from Numerical recipes
* @param data array of nx x n data points<br>
* On return contains transform <br>
* For packing see Numerical Recipes.
* Note: convention for ny is as in Numerical Recipes, not as in realftx.for
* @param    nx number of data points in x direction
* @param    ny number of data points in y direction
* @param    isign 1 for forward; -1 for inverse
*/          
//**********************************************************************          
      private void realfty(double data[][], int nx, int ny, int isign)   {
      
      int i,i1,i2,i3,i4,np3,k;
      double c1=0.5,c2,h1r,h1i,h2r,h2i;
      double wr,wi,wpr,wpi,wtemp,theta;

      theta=3.141592653589793/(double) (ny>>1);
      if (isign == 1) {
            c2 = -0.5;
            four1y(data,nx,ny,1);
      } else {
            c2=0.5;
            theta = -theta;
      }
      wtemp=Math.sin(0.5*theta);
      wpr = -2.0*wtemp*wtemp;
      wpi=Math.sin(theta);
      wr=1.0+wpr;
      wi=wpi;
      np3=ny+3;
      for (i=2;i<=(ny>>2);i++) {
            i4=1+(i3=np3-(i2=1+(i1=i+i-1)));
            for(k=1;k<=nx;k++) {
                  h1r=c1*(data[k][i1]+data[k][i3]);
                  h1i=c1*(data[k][i2]-data[k][i4]);
                  h2r = -c2*(data[k][i2]+data[k][i4]);
                  h2i=c2*(data[k][i1]-data[k][i3]);
                  data[k][i1]=h1r+wr*h2r-wi*h2i;
                  data[k][i2]=h1i+wr*h2i+wi*h2r;
                  data[k][i3]=h1r-wr*h2r+wi*h2i;
                  data[k][i4] = -h1i+wr*h2i+wi*h2r;
            }      
            wr=(wtemp=wr)*wpr-wi*wpi+wr;
            wi=wi*wpr+wtemp*wpi+wi;
      }
      if (isign == 1) {
            for(k=1;k<=nx;k++) {
                  data[k][1] = (h1r=data[k][1])+data[k][2];
                  data[k][2] = h1r-data[k][2];
            }
      } else {
            for(k=1;k<=nx;k++) {
                  data[k][1]=c1*((h1r=data[k][1])+data[k][2]);
                  data[k][2]=c1*(h1r-data[k][2]);
            }
            four1y(data,nx,ny,-1);
      }
}

}
     
 
 

  class Lattice {
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
      protected int plotType;
      protected myPallette myPal;                 // Pallette for plotting
      protected int i,j,k;
      
      protected double[][][] psip;
      
                
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  int in_plotType) {
                  
            nx = width;
            ny = height;
            psip = new double[fields][width+2][height+2];
                                             
            
            parameters = new double[nParameters];
            for(i=1;i<nParameters;i++) 
                parameters[i]=in_parameters[i];            
            
            dt=in_dt;
            plotType=in_plotType;
            setPallette();
      }         
      
          
      // Set random initial conditions specified by ic[][]
      // Can override for other i.c. if desired
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
        for(j=1;j<=ny;j++) {
            for(i=1;i<=nx;i++) {
                for(k=0;k<fields;k++) {
                   psi[k][i][j]=icp[k][0]*(Math.random()-0.5)+icp[k][1];
                }   
            }
        }
        setBoundaryConditions(psi, nx, ny, fields);
      }                                            

      // Dummy time stepper. 
      public double tstep(double[][][] psi, double t) {
            return t;
      } 
       
      
     public double[][][] setBoundaryConditions(double[][][] psi, int nx, int ny, int fields) {
        int i,j,k;
 
        for(i=0;i<fields;i++) {
            for(j=1;j<=nx;j++) {
                psi[i][j][0]=psi[i][j][ny];
                psi[i][j][ny+1]=psi[i][j][1];
            }
            for(k=1;k<=ny;k++) {
                psi[i][0][k]=psi[i][nx][k];
                psi[i][nx+1][k]=psi[i][1][k];
            }
        psi[i][0][0]=0;
        psi[i][nx+1][0]=0;
        psi[i][0][ny+1]=0;
        psi[i][nx+1][ny+1]=0;
        } 
                       
        return psi;
     }                     
      
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
 * @(#)SH.java  5/1/99
 *
 * Copyright (c) 1999, Michael Cross All Rights Reserved.
 *
 * Time stepper for Lifshitz-Petrich modification of theSwift-Hohenberg Equation
 * using pseudo-spectral method
 * NB: extends spectralPDE
 */
 

  class Lifshitz extends spectralPDE {
      private int i,j,k;     
      private double eps,g1,q1;            // convenience notation for parameters of equation 
            
      // Consructor
      public Lifshitz() {
      
            // All the fields in the Constructor are REQUIRED
            fields=1;                   // Number of fields
            spectral=true;              // True if FFT plots may be shown (spectral)
            numPlotTypes=1;             // Number of plot types
      // Default plots of first field are made in makePlot and makeFFTPlot
      // methods in spectralPDE.java. These can be overwritten here for fancier
      // plots or plots of other fields.
            
            // Parameters of equation
            nParameters=3;
            defaultValues=new String[nParameters];
            defaultValues[0]="0.";
            defaultValues[1]="1.";
            defaultValues[2]="1.9318";
            labels = new String[nParameters];
            labels[0]=" eps ";
            labels[1]="alpha";
            labels[2]="  q  ";             
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            eps=parameters[0];
            g1=parameters[1];
            q1=parameters[2];
     }
     
     // Choose pallette for plot: choice may depend on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette () {      
            myPal = new RGBPallette();
     }     

     // Construct multipliers of linear terms in transform equation at wavevector k
     protected double[] getArg(double k) {
        double[] d=new double[fields];
        d[0]=eps-Math.pow(k*k-1,2)*Math.pow(k*k-q1*q1,2);
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
               
}   




/*
 * @(#)QUAD.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Time stepper for lattice of quadratic maps
 */
 

/*
 * @(#)RNL.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Time stepper for lattice of Complex Ginzburg Landau oscillators
 */
 
/*
 * @(#)Phase.java  2/16/04
 *
 * Copyright (c) 2004, Michael Cross All Rights Reserved.
 *
 * Time stepper for lattice of phase oscillators
 */
 

class Phase extends CODE {
      private double b,w;
      private int i,j,k;
      private double[][] om; 
      private static final double Pi2=2*Math.PI;   
      
      
      // Consructor
      public Phase() {
      
            // Number of fields
            fields=1;
            numPlotTypes=1;
            spectral=false; 
            nInitialParameters=0;   
            
            // Parameters of equation
            nParameters=2;
            defaultValues=new String[nParameters];
            defaultValues[0]="1";
            defaultValues[1]="1";
            labels = new String[nParameters];
            labels[0]="  K  ";        // alpha 
            labels[1]=" w ";
      }
      
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  int in_plotType) {
                  
            super.init(width, height, in_parameters, in_dt, in_plotType);
            w=in_parameters[1];
            om= new double[nx+2][ny+2];              
    }             
            
                       

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            b=in_parameters[0]/4;
            w=in_parameters[1];
     }
         
     public void derivs(double[][][] psi, double[][][] dpsi, double t, int nx, int ny, int fields){

           for(i=1;i<=nx;i++)
             for(j=1;j<=ny;j++)        
               dpsi[0][i][j] = om[i][j] +
                            b*(Math.sin(psi[0][i-1][j]-psi[0][i][j])
                             + Math.sin(psi[0][i+1][j]-psi[0][i][j])
                             + Math.sin(psi[0][i][j-1]-psi[0][i][j])
                             + Math.sin(psi[0][i][j+1]-psi[0][i][j]));
      }
      
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
        for(i=1;i<=nx;i++) {
           for(j=1;j<=ny;j++) {
                om[i][j]=w*(Math.random()-0.5);
                psi[0][i][j]=Pi2*Math.random();
           }
        }
      }             
    
          
     // Choose what to plot: return field to plot in psir[0]
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {  
        for(i=1;i<=nx;i++) {
           for(j=1;j<=ny;j++) {
                psir[0][i][j]=mod(psir[0][i][j],Pi2);
           }
        }                 
     }

     private double mod(double x, double shift) {
         while (x>shift) {
            x=x-shift;
         }   
         while (x<0.) {
            x=x+shift;
         }
         return x;
      }     
     
     // Choose pallette for plot based on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette() {      
        myPal = new circPallette();
     }
     
}

class RNL extends CODE {
      private double a,b,w,square;
      private int i,j,k;
      private double[][] om;    
      
      
      // Consructor
      public RNL() {
      
            // Number of fields
            fields=2;
            numPlotTypes=2;
            spectral=false;    
            
            // Parameters of equation
            nParameters=3;
            defaultValues=new String[nParameters];
            defaultValues[0]="1.25";
            defaultValues[1]="1";
            defaultValues[2]="1";
            labels = new String[nParameters];
            labels[0]="  "+"\u03B1"+"  ";        // alpha 
            labels[1]="  "+"\u03B2"+"  ";       // beta 
            labels[2]=" w ";
      }
      
      public void init(int width, int height, double[] in_parameters, double in_dt,
                  int in_plotType) {
                  
            super.init(width, height, in_parameters, in_dt, in_plotType);
            w=parameters[2];
            om= new double[nx+2][ny+2];              
    }             
            
                       

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            a=parameters[0];
            b=parameters[1]/4;
            w=parameters[2];
     }
         
     public void derivs(double[][][] psi, double[][][] dpsi, double t, int nx, int ny, int fields){

           for(i=1;i<=nx;i++) {
             for(j=1;j<=ny;j++) {        
               square = Math.pow(psi[0][i][j],2)+Math.pow(psi[1][i][j],2);
               dpsi[0][i][j] = (1.0-square)*psi[0][i][j]-(om[i][j]+a-a*square)*psi[1][i][j]
                            -b*(psi[1][i-1][j]+psi[1][i+1][j]+psi[1][i][j-1]+psi[1][i][j+1]
                                -4*psi[1][i][j]);
               dpsi[1][i][j] = (1.0-square)*psi[1][i][j]+(om[i][j]+a-a*square)*psi[0][i][j]
                            +b*(psi[0][i-1][j]+psi[0][i+1][j]+psi[0][i][j-1]+psi[0][i][j+1]
                                -4*psi[0][i][j]);               
          }
       }          
     
      }
      
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
        super.initialCondition(psi, icp, nicp);
        for(i=1;i<=nx;i++)
           for(j=1;j<=ny;j++)
                om[i][j]=w*(Math.random()-0.5);
      }             
    
          
     // Choose what to plot: return field to plot in psir[0]
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {

          int i,j,k;
           for(j=1;j<=plotheight;j++) {
             for(i=1;i<=plotwidth;i++) {
                if(plotType==0) 
                   psir[0][i][j] = Math.sqrt(Math.pow(psir[0][i][j],2)+Math.pow(psir[1][i][j],2));
                else  
                   psir[0][i][j] = Math.atan2(psir[1][i][j],psir[0][i][j]);
             }
          }                  
     }

     
     
     // Choose pallette for plot based on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette() {      
            if(plotType==0) 
                  myPal = new greyPallette();
            else
                  myPal = new circPallette();
     }
     
}

class QUAD extends Lattice {
      private double a,D;
      private int i,j,k;     
      
      
      // Consructor
      public QUAD() {
      
            // Number of fields
            fields=1;
            numPlotTypes=1;
            spectral=false;
            nInitialParameters=1; 
            
            // Parameters of equation
            nParameters=2;
            defaultValues=new String[nParameters];
            defaultValues[0]="4";
            defaultValues[1]="1";
            labels = new String[nParameters];
            labels[0]=" a ";             
            labels[1]=" D ";
      }     

     // Grab parameters for equation
     public void setParameters(int nParameters, double[] in_parameters) {            
            super.setParameters(nParameters, in_parameters);
            a=parameters[0];
            D=parameters[1];
     }
     
      // Time stepper. 
      public double tstep(double[][][] psi, double t) {
                   
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                  psip[i][j][k]=a*psi[i][j][k]*(1.0-psi[i][j][k]);
            for(i=0;i<fields;i++)
              for(j=1;j<=nx;j++)
                for(k=1;k<=ny;k++)
                   psi[i][j][k]=psip[i][j][k]+
                    D*(psip[i][j-1][k]+psip[i][j+1][k]+psip[i][j][k-1]+psip[i][j][k+1]
                                -4*psip[i][j][k])/4;;             
            // Make sure boundary conditions still hold
            setBoundaryConditions(psi, nx, ny, fields);                            
            t = t + 1;
            return t;
      }      
      
public void derivs(double[][][] psi, double[][][] dpsi, double t, int nx, int ny, int fields){}        
          
      // Set random initial conditions specified by ic[][]
      // Can override for other i.c. if desired
      public void initialCondition(double[][][] psi, double[][] icp, int nicp) {
        for(j=1;j<=ny;j++) {
            for(i=1;i<=nx;i++) {
                for(k=0;k<fields;k++) {
                   psi[k][i][j]=icp[k][0]*Math.random();
                }   
            }
        }
        setBoundaryConditions(psi, nx, ny, fields);
      }         
    
          
     // Choose what to plot: return field to plot in psir[0]
     public void makePlot(double[][][] psir, int plotwidth, int plotheight) {
               
     }

     
     
     // Choose pallette for plot based on integer plotType set in html file
     // Choices are grey, circ (circular rainbow) and RGB
     public void setPallette() {      
            if(plotType==0) 
                  myPal = new RGBPallette();
            else
                  myPal = new circPallette();
     }
     
}

