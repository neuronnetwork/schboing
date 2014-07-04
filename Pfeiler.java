
 /*
 * @(#   Pfeiler.java  
 *
 * Copyright (c) 2014, Jean-Michel Lorenzi All Rights Reserved.
 *
 * PDS mit RBN
 */ 
 
 
import java.util.*;
import java.lang.Math;   
import static dissipativeneuron.Entwicklung.*;                               
 

 
class Pfeiler{
	static final int MaxZahlVonFeldern=15;
	public double[][][] pds;
	int Breite;
	int ZahlVonFeldern;
	
	Pfeiler(int ZFeldern,int nx, int ny){
		if( ny!=nx )  
					throw new AssertionError("Nur Quadraten wurden implementiert!");
		
		if (CROSS_TABELLEN)
				Breite=nx+1;
		else
				Breite=nx;
			
		ZahlVonFeldern=ZFeldern;
		if(ENTWICKLUNG_MODUS) 
				if( (ZahlVonFeldern<1) ||  (ZahlVonFeldern>MaxZahlVonFeldern) )  
					throw new AssertionError("ZahlVonFeldern, "+ZahlVonFeldern +" paßt nicht!");  
		

		pds=new double[ZahlVonFeldern][Breite][Breite]; 		 
	} 
	
	private Pfeiler(){
		ZahlVonFeldern=MaxZahlVonFeldern;
		pds=new double[ZahlVonFeldern][][]; 
		if(!ENTWICKLUNG_MODUS)   
					throw new AssertionError("Nicht sauber!");  
		else {
		Breite=BREITE;
		if (CROSS_TABELLEN)
			Breite++;  
		pds=new double[ZahlVonFeldern][Breite][Breite];  
		}
	}
	 
	double[][][] nimm_Feld(){//nicht benutzt
		return pds;
	}
	
	void reinit(int ZFeldern,int nx, int ny){ 
		if( ny!=nx )  
					throw new AssertionError("Nur Quadraten wurden implementiert!");
		
		if(ENTWICKLUNG_MODUS) 
				if( (ZFeldern<0) || !(ZFeldern<MaxZahlVonFeldern) )  
					throw new AssertionError("ZahlVonFeldern, "+ZFeldern +" paßt nicht!");  
		for (int i=0;i<ZFeldern;i++){
			for (int x=0;x<Breite;x++) 
					pds[i][x]=null; //Auf java muß ich überprüfen, daß kein anderer Pointer den gleichen Gegenstand bedeutet.
			pds[i]=null;
		}
		pds=null; 
	
		
		ZahlVonFeldern=ZFeldern;
		if (CROSS_TABELLEN)
				Breite=nx+1;
		else
				Breite=nx; 

		pds=new double[ZahlVonFeldern][Breite][Breite];    
	}
	  
	public synchronized void Unruhe(){   
				if(NN.SCHWARZWPRUEFUNG){
 					//for(int k=0;k<ZahlVonFeldern;k++)
					for(int x=0;x<RBNGROESSE;x++)
						for(int y=0;y<RBNGROESSE;y++)  
							pds[0][x][y]=0;
					int ZahlVonPunkte=55;
					if(ZahlVonPunkte!=0)
						for (int u=0;u<ZahlVonPunkte;u++){
							short x=(short)(double)((Math.random()*0.3+0.2)*RBNGROESSE);
							short y=(short)(double)((Math.random()*0.3+0.2)*RBNGROESSE);
							if(pds[0][x][y]==1.)
								u--;
							pds[0][x][y]=1.;	
						}
					else
					if(ZahlVonPunkte==0)
					for(int x=0;x<  RBNGROESSE;x++){
						for(int y=(int)((float)(RBNGROESSE)*0.2);y<0.3* (float)(RBNGROESSE);y++)  //	for(int y=0;y<Breite;y++)  
							pds[0][x][y]=(Math.random()>0.5)?0.:1.;								
						} 		
				}
								
/* 				 {//waschen   //doesn´t work
						int nb=-40;
						for(int i=0;i<RBNGROESSE;i++)
							for(int j=0;j<RBNGROESSE;j++)
								if(rbn.pds[0][ i ][j ]>.5 ) 
									nb++; 
						for(int i=0;i<RBNGROESSE/3;i++)
							if(nb>=0)
								for(int j=0;j<RBNGROESSE;j++){
									if(rbn.pds[0][ i ][j ]>.5 ) 
										nb--;  
									rbn.pds[0][ i ][j ]=0; 
								}
						if(false)for(int i=2*RBNGROESSE/3;i<RBNGROESSE/3;i++)
							if(nb>=0)
								for(int j=0;j<RBNGROESSE;j++){
									if(rbn.pds[0][ i ][j ]>.5 ) 
										nb--;  
									rbn.pds[0][ i ][j ]=0;
									if(nb<1)
										break; 
								}
						if(false)for(int i=0;i<RBNGROESSE;i++)
							if(nb>=0)
								for(int j=0;j<RBNGROESSE;j++){
									if(rbn.pds[0][ i ][j ]>.5 ) 
										nb--;  
									rbn.pds[0][ i ][j ]=0;
									if(nb<1)
										break;  
								}
		}
	 */	

			if(!NN.SCHWARZWPRUEFUNG) //for(int k=0;k<ZahlVonFeldern;k++)
			synchronized(pds[0][0] ){  
					for(int x=0;x< Breite;x++) 
						for(int y=0;y<Breite;y++)  
							pds[0][x][y]=0; 
					for(int x=Breite/2;x< Breite;x++) 
						for(int y=Breite/2;y<Breite;y++)  
							pds[0][x][y]=(Math.random()>0.5)?0:+0.0000000001;   
					}
		 

	if(false)	 				for(int x=0;x< Breite;x++){
						for(int y=0;y<Breite;y++)  
							pds[0][x][y]=(Math.random()>0.5)?0.:1.; 
								}

	}

	public boolean Ja_oder_Nein(int x, int y){
		return pds[0][x][y]>0.5;
	}
}

class Sternzelle{  
	public double Aminoessigsäure;
	public double[] Staerke;
//	short[] NummerVonBeLpn;sehe unten
	int ZahlVonBeLpn;
	Be_lpn[] a_beLpn;  
	private  Pfeiler[]  Hautp;

	
	// public Sternzelle(){
		// ZahlVonBeLpn=0; 
		// NummerVonBeLpn=new short[100];
		//Staerke...
	// }
		
	public Sternzelle(Be_lpn[] a_belpn_,int ZahlVonBeLpn_,Pfeiler Haut){
		ZahlVonBeLpn=ZahlVonBeLpn_; 
		a_beLpn=a_belpn_;
//		NummerVonBeLpn=new short[ZahlVonBeLpn];		for(int l=0;l<ZahlVonLpn;l++)			a_be_Lpnek
		Staerke=new double[ZahlVonBeLpn];
		for(int l=0;l<ZahlVonBeLpn;l++)	
			Staerke[l]=1.;
			
		Hautp=new Pfeiler[]{Haut};
	 	if(  Hautp[0]!=Haut)throw new AssertionError("  den_Wert_          rechnen?!");  
		 
	} 
	
	public void rechnen(){
		Aminoessigsäure=0;
		double Summe=0;
		for(int l=0;l<ZahlVonBeLpn;l++){
				double Enttaeuschung;
				{
					Be_lpn derLPN=a_beLpn[l];
					Enttaeuschung=derLPN.Wert
						-nimmHaut().pds[0][derLPN.xStelleVonKoerper][derLPN.yStelleVonKoerper];
				}
				if(true)//ungerade
					Enttaeuschung=Math.pow(Enttaeuschung,2);
				else	{
					//Enttaeuschung=Math.pow(Enttaeuschung,1);
					//Enttaeuschung=Math.pow(Enttaeuschung,3);
					Enttaeuschung=Math.abs(Enttaeuschung);
				}
				 //????Enttaeuschung/=Breite
				 Summe+=Staerke[l]*Enttaeuschung;
			 	}; 
		//????Summe/=Breite
		Aminoessigsäure = Math.exp(-Summe
									//*0.2
									);
	}	
	
	public Pfeiler nimmHaut(){
		return Hautp[0];
	}

}

class Lpn{
	public double        affinerBeiwert;
	public double[] Staerke;
	short[] xZweigstelle;  
	short[] yZweigstelle;  
	short      zZweigstelle;
	short xStelleVonKoerper;
	short yStelleVonKoerper;
	public short zStelleVonKoerper;
	public double Wert;	 
	
    private  Pfeiler[]  Hautp;
	
	protected Lpn(Pfeiler h){//protects zStelleVonKoerper
		Hautp=new Pfeiler[]{h}; 
		affinerBeiwert=0.;
				
		Staerke=new double[PRESY_NB];
		xZweigstelle=new short[PRESY_NB];
		yZweigstelle=new short[PRESY_NB];
		zZweigstelle=(short)(0);
			for(short d=0;d<PRESY_NB;d++){
				xZweigstelle[d]=(short)(double)(Versuchbreite*Math.random());
				yZweigstelle[d]=(short)(double)(Versuchbreite*Math.random());
				Staerke[d]		=(Math.random()*2-1)*100;
		}
		
		xStelleVonKoerper=(short)(int)(Math.random()*Versuchbreite);
		yStelleVonKoerper=(short)(int)(Math.random()*Versuchbreite); 
		Wert=0.5;
		
	}
	
	public Pfeiler nimmHaut(){
		return Hautp[0];
	}

	
	public void den_Wert_rechnen(){
			if(2+2==4)throw new AssertionError("  den_Wert_          rechnen?!");  
		 
		Pfeiler Haut=nimmHaut();
		Wert=0;
		for(int in=0;in<3;in++)
			;//Wert+=(Haut.pds[0][von_x[in]][von_y[in]] -   0.5 ) * Staerke[in] ;
	}
	
}

class Be_lpn extends Lpn{ 
	public Be_lpn(Pfeiler h){
		super(h);
		zStelleVonKoerper=0;
	}		
}

class Ki_lpn extends Lpn{ 
	public Ki_lpn(Pfeiler h){
		super(h);
		zStelleVonKoerper=3;
	}		
}