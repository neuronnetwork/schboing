/*
 * @(#   NN.java  
 *
 * Copyright (c) 2014, Jean-Michel Lorenzi All Rights Reserved.
 *
 *  
 */ 

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static dissipativeneuron.Entwicklung.*;    
//import static dissipativeneuron.Mathematik.*;     

public class NN{ 

	Pfeiler  Haut;  
	int SternzellZahl;
	public int NumberOfLpn; 
	Be_lpn[] be_lpn;
	Ki_lpn[] ki_lpn;
	Sternzelle[] sz;
	Pfeiler rbn;
	double force;
	boolean [ ][ ] zukuenftige; 
	static boolean Reaktion,Entspannung,tanulj,Galileo,UEBERPRUEFUNG_VON_BM,SCHWARZWPRUEFUNG,uebereinstimmen;
	public topologischeBoltzmannmaschine[] BM;
	public int Zahl_von_der_BM;
	public topologischeBoltzmannmaschine EBM,MBM;
	public Interface Schnittstelle0;
	public Interface2 Schnittstelle;  
	private double alles_ist_null=0;//für die Überprüfung neuer Beziehungen
	 
	
	public  NN(Pfeiler pf){
		Entspannung=true;
		tanulj=false;
		Galileo=true; 
		UEBERPRUEFUNG_VON_BM = true;  
		SCHWARZWPRUEFUNG = true; 
		if(SCHWARZWPRUEFUNG)
			Reaktion =false;
		else
			Reaktion=true; 
		uebereinstimmen = true;  
		Haut=pf;
		Haut.Unruhe(); 
		
		zukuenftige=new boolean[RBNGROESSE][RBNGROESSE];
		
		NumberOfLpn=Versuchbreite*Versuchbreite//0. Stock
						+Versuchbreite*Versuchbreite//1.Stock
						+Versuchbreite*Versuchbreite //2.Stock  
						+Versuchbreite*Versuchbreite;//3.Stock  
		be_lpn=new Be_lpn[NumberOfLpn];
		for (int a=0;a< NumberOfLpn;a++)
			be_lpn[a]=new Be_lpn(Haut);
		ki_lpn=new Ki_lpn[NumberOfLpn];
		for (int a=0;a< NumberOfLpn;a++)
			ki_lpn[a]=new Ki_lpn(Haut);
		  
		ziehen();
		
		
		
	{SternzellZahl=1;
		sz=new Sternzelle[SternzellZahl];
		for (int a=0;a<SternzellZahl;a++)
			sz[a]=new Sternzelle(be_lpn,NumberOfLpn,Haut);
		
		{/*for (int a=0;a<SternzellZahl;a++)
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){
				sz[a].xZweigstelle[x][y]=(short)(int)(x+20);
				sz[a].yZweigstelle[x][y]=y;
		}
		for (int a=0;a<SternzellZahl;a++){
			sz[a].Staerke[0][0]=+1;
			sz[a].Staerke[1][0]=+1;
			sz[a].Staerke[0][1]=+1;
			sz[a].Staerke[1][1]=+0.;
		} 
		sz[0].Staerke[0][1]=-0;
		sz[1].Staerke[1][0]=-0;
		sz[2].Staerke[0][0]=-0; 
		

		}
		*/
		}
	}


	 initValues();
	} 

	public void connect_to_others(int u){
		connect_to_others(u,0);
	}
	public void connect_to_others(int u,double r){
		for (int xy=Versuchbreite*Versuchbreite;xy<PRESY_NB;xy++ ){
			short x=(short)(double)((Math.random()*0.3+0.2)*RBNGROESSE);
			short y=(short)(double)((Math.random()*0.3+0.2)*RBNGROESSE);
			be_lpn[u].xZweigstelle[xy]=x;
			be_lpn[u].xZweigstelle[xy]=x;
			be_lpn[u].Staerke[xy]=Math.random()*r;
			}
	} 

	public void ziehen(){
	


		short s,b;
		{//0. Stock
		s=0; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){  
				be_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)(x);
				be_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				be_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		} 
		}
		
		{//1. Stock
		s=1; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){ 
				be_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)(x);
				be_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				be_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		}
		
		{int a=NummerDerLpn(1,1);
		for(short xy=0;xy<Versuchbreite+Versuchbreite;xy++) 
				be_lpn[b+NummerDerLpn(1,1)].Staerke[xy]=0;
		be_lpn[b+a].Staerke[1+Versuchbreite*1]=1;
		connect_to_others(b+a);	 
		}
		for (int a=0;a<Versuchbreite*Versuchbreite-1;a++){
			be_lpn[b+a].Staerke[0+Versuchbreite*0]=+0.5;
			be_lpn[b+a].Staerke[1+Versuchbreite*0]=+0.5;
			be_lpn[b+a].Staerke[0+Versuchbreite*1]=+0.5;
			be_lpn[b+a].Staerke[1+Versuchbreite*1]=+0.;
			for(short x=0;x<Versuchbreite;x++)
				for(short y=0;y<Versuchbreite;y++){ 
					be_lpn[b+a].xZweigstelle[x+Versuchbreite*y]=x;
					be_lpn[b+a].yZweigstelle[x+Versuchbreite*y]=y;
					be_lpn[b+a].zZweigstelle=(short)(int)(s-1);
			}
			connect_to_others(b+a); 
			}
		{ //Korrigierung
		be_lpn[b+NummerDerLpn(0,0)].Staerke[0+Versuchbreite*1]=-0.5;
		be_lpn[b+NummerDerLpn(1,0)].Staerke[1+Versuchbreite*0]=-0.5;
		be_lpn[b+NummerDerLpn(0,1)].Staerke[0+Versuchbreite*0]=-0.5;	
		be_lpn[b+NummerDerLpn(1,1)].Staerke[1+Versuchbreite*1]=1.0;
		} 	
	
		}
		
		{//2. Stock
		s=2; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){ 
				be_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)x;
				be_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				be_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		}
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){  
				int a=NummerDerLpn(x,y);
				for(short xx=0;xx<Versuchbreite;xx++)
					for(short yy=0;yy<Versuchbreite;yy++){   
						be_lpn[b+a].Staerke[xx+Versuchbreite*yy]=0; 
						be_lpn[b+a].xZweigstelle[xx+Versuchbreite*yy]=xx;
						be_lpn[b+a].yZweigstelle[xx+Versuchbreite*yy]=yy;
						be_lpn[b+a].zZweigstelle=(short)(int)(s-1);
					}
				connect_to_others(b+a);	 
				}
		{ //Korrigierung
		be_lpn[b+NummerDerLpn(0,0)].Staerke[0+Versuchbreite*0]=1.0;
		be_lpn[b+NummerDerLpn(1,0)].Staerke[1+Versuchbreite*0]=1.0;
		be_lpn[b+NummerDerLpn(0,1)].Staerke[0+Versuchbreite*1]=1.0;
		be_lpn[b+NummerDerLpn(1,1)].Staerke[1+Versuchbreite*1]=1.0;
		} 
		}
		{//2. Stock ki_lpn	
		s=2; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){ 
				ki_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)(x);
				ki_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				ki_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		}
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++)
				ki_lpn[b+NummerDerLpn(1,1)].Staerke[x+Versuchbreite*y]=0;
		ki_lpn[NummerDerLpn(1,1)].Staerke[1+Versuchbreite*1]=1;
		for (int a=0;a<Versuchbreite*Versuchbreite-1;a++){
			ki_lpn[b+a].Staerke[0+Versuchbreite*0]=+0.5;
			ki_lpn[b+a].Staerke[1+Versuchbreite*0]=+0.5;
			ki_lpn[b+a].Staerke[0+Versuchbreite*1]=+0.5;
			ki_lpn[b+a].Staerke[1+Versuchbreite*1]=+0.;
			for(short x=0;x<Versuchbreite;x++)
				for(short y=0;y<Versuchbreite;y++){ 
					ki_lpn[b+a].xZweigstelle[x+Versuchbreite*y]=x;
					ki_lpn[b+a].yZweigstelle[x+Versuchbreite*y]=y;
					ki_lpn[b+a].zZweigstelle=(short)(int)(s+1);
			}
		}
		{ //Korrigierung
		ki_lpn[b+NummerDerLpn(0,0)].Staerke[0+Versuchbreite*1]=-0.5;
		ki_lpn[b+NummerDerLpn(1,0)].Staerke[1+Versuchbreite*0]=-0.5;
		ki_lpn[b+NummerDerLpn(0,1)].Staerke[0+Versuchbreite*0]=-0.5;	
		ki_lpn[b+NummerDerLpn(1,1)].Staerke[1+Versuchbreite*1]=1.0;
		} 
		} 

		{//3. Stock , be: leer
		s=3; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){ 
				be_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)x;
				be_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				be_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		}  		
		
		{
			for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++)
				for(short xx=0;xx<Versuchbreite;xx++)
					for(short yy=0;yy<Versuchbreite;yy++)  {  
						int a=NummerDerLpn(x,y);
						be_lpn[b+a].Staerke[xx+Versuchbreite*yy]=0; 
						be_lpn[b+a].xZweigstelle[xx+Versuchbreite*yy]=xx;
						be_lpn[b+a].yZweigstelle[xx+Versuchbreite*yy]=yy;
						be_lpn[b+a].zZweigstelle=(short)(int)(s-1);
					}
		if(false){ //Korrigierung
		be_lpn[b+NummerDerLpn(0,0)].Staerke[0+Versuchbreite*0]=1.0;
		be_lpn[b+NummerDerLpn(1,0)].Staerke[1+Versuchbreite*0]=1.0;
		be_lpn[b+NummerDerLpn(0,1)].Staerke[0+Versuchbreite*1]=1.0;
		be_lpn[b+NummerDerLpn(1,1)].Staerke[1+Versuchbreite*1]=1.0;
		} 
		}
		}
 		
			 		
		if(true){//Ô fog tanulni.
				double Beginn=0.03;
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
		kleinen_Fehler();
				{//a befülkek rosszok és ô fog tanulni. 
				s=1; 
				b=(short)(int)(s*Versuchbreite*Versuchbreite);
		
				for (int a=0;a<Versuchbreite*Versuchbreite;a++){
					
					for(short xx=0;xx<Versuchbreite;xx++)
						for(short yy=0;yy<Versuchbreite;yy++)
							be_lpn[b+a].Staerke[xx+Versuchbreite*yy]=(Math.random()*2-1)*Beginn; 		
					connect_to_others(b+a,0.9);	 
				}
				tanulj=true;
				}
				
		
			{//  Die linearen Beiwerte sind falsch. Ô fog tanulni.
				s=1; 
				b=(short)(int)(s*Versuchbreite*Versuchbreite);
		
				for (int a=0;a<Versuchbreite*Versuchbreite;a++){
					
					for(short xx=0;xx<Versuchbreite;xx++)
						for(short yy=0;yy<Versuchbreite;yy++)
							be_lpn[b+a].affinerBeiwert=(Math.random()*2-1)*Beginn; 
				}
				tanulj=true;
					
			}	
		}

 
		if(false){//ARRAY AOUT OF BOUND//4. Stock
		s=4; 
		b=(short)(int)(s*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++){ 
				be_lpn[b+NummerDerLpn(x,y)].xStelleVonKoerper=(short)(int)(x);
				be_lpn[b+NummerDerLpn(x,y)].yStelleVonKoerper=(short)(int)(y+Versuchbreite*s*2);
				be_lpn[b+NummerDerLpn(x,y)].zStelleVonKoerper=(short)(int)s;
		}
		for (int a=0;a<Versuchbreite*Versuchbreite;a++){
			be_lpn[b+a].Staerke[0+Versuchbreite*0]=+0. ;
			be_lpn[b+a].Staerke[1+Versuchbreite*0]=+0. ;
			be_lpn[b+a].Staerke[0+Versuchbreite*1]=+0. ;
			for(short x=0;x<Versuchbreite;x++)
				for(short y=0;y<Versuchbreite;y++){ 
					be_lpn[b+a].xZweigstelle[x+Versuchbreite*y]=x;
					be_lpn[b+a].yZweigstelle[x+Versuchbreite*y]=y;
					be_lpn[b+a].zZweigstelle=0;
			}
		}
		{ //Korrigierung
		be_lpn[b+NummerDerLpn(0,0)].Staerke[0+Versuchbreite*1]=-0.5;
		be_lpn[b+NummerDerLpn(1,0)].Staerke[1+Versuchbreite*0]=-0.5;
		be_lpn[b+NummerDerLpn(0,1)].Staerke[0+Versuchbreite*0]=-0.5;
		} 
		}

	}	

	
	public void kleinen_Fehler(){kleinen_Fehler(1);}
	public void kleinen_Fehler(double wie_viel)
	{ 
		if(UEBERPRUEFUNG_VON_NN)
			{
			double change=0.4*wie_viel;
			for (int a=0;a< NumberOfLpn;a++) 
				{
					for(short c=0;c<PRESY_NB;c++) 
							be_lpn[a].Staerke[c]  +=(Math.random()*2-1)*change; 
					connect_to_others(a,change);		 
					be_lpn[a].affinerBeiwert		    +=(Math.random()*2-1)*change; 
				}
			}
		if(UEBERPRUEFUNG_VON_BM)
			{ 
			if(!(Zahl_von_der_BM>0))
				return ;//throw new AssertionError("Es gibt keine Maschinen!");
			for(int jj=0;jj<Zahl_von_der_BM;jj++)
				{
				Boltzmannmaschine M=BM[jj];
				for(int a=0;a<M.Zahl_Von_Beziehungen;a++)
					M.Beziehungen[a].Ruhe();
				}
			for(int jj=0;jj<Zahl_von_der_BM;jj++) 
				BM[jj].schuetteln(wie_viel*alles_ist_null);
			}
	}
	
	
	
	public void tanul(){
		tanul(-1);
	}
	
	static int Beispiel;
	
	public void tanul(int n){
	boolean same=true;
	short last=Versuchbreite*3*2;
	final int BeispielNumber=3; 
	
	if(n==-1){
		if(Math.random()*100<20)
			Beispiel=(int)(long)(Math.round(Math.random()*BeispielNumber));
		n=Beispiel;
	}
				
	switch(n){
		case 1://senkrecht
		Haut.pds[0][0][0]=1.;  
		Haut.pds[0][1][0]=0.;  
		Haut.pds[0][0][1]=1.;  
		Haut.pds[0][1][1]=0.;  
	if(same){
		Haut.pds[0][0][last]=1.;  
		Haut.pds[0][1][last]=0.;  
		Haut.pds[0][0][last+1]=1.;  
		Haut.pds[0][1][last+1]=0.;  }
		break;
		case 0://waagerecht 
		Haut.pds[0][0][0]=1.;  
		Haut.pds[0][1][0]=1.;  
		Haut.pds[0][0][1]=0.;  
		Haut.pds[0][1][1]=0.;   
	if(same){
		Haut.pds[0][0][last]=1.;  
		Haut.pds[0][1][last]=1.;  
		Haut.pds[0][0][last+1]=0.;  
		Haut.pds[0][1][last+1]=0.;  } 
		break;
		case 2://schräg
		Haut.pds[0][0][0]=0.;  
		Haut.pds[0][1][0]=1.;  
		Haut.pds[0][0][1]=1.;  
		Haut.pds[0][1][1]=0.;   
	if(same){
		Haut.pds[0][0][last]=0.;  
		Haut.pds[0][1][last]=1.;  
		Haut.pds[0][0][last+1]=1.;  
		Haut.pds[0][1][last+1]=0.;  }
		break;
		case 3://einen Punkt
		Haut.pds[0][0][0]=0.;  
		Haut.pds[0][1][0]=0.;  
		Haut.pds[0][0][1]=0.;  
		Haut.pds[0][1][1]=1.;   
	if(same){
		Haut.pds[0][0][last]=0.;  
		Haut.pds[0][1][last]=0.;  
		Haut.pds[0][0][last+1]=0.;  
		Haut.pds[0][1][last+1]=1.;  }
	//default:
		}
	
	}
	
	public void  initValues(){
		if( UEBERPRUEFUNG_VON_NN)
			tanul();
				
		///rbn:
		if(rbn!=null)
		if(!ptrFertig){//rbn pointer ist noch nicht fertig
		ptrFertig=true;
		rbn.Unruhe(); 
			
		}
		{//E- und MBM
		
		if(nurEBM)
			Zahl_von_der_BM=1;
		else
			Zahl_von_der_BM=2;
		BM=new topologischeBoltzmannmaschine[Zahl_von_der_BM]; 
		int Reihen=20;
		boolean waagerechte_Linie=true;
		boolean genagelte_Linie=true;// laufend6.6.2014
		boolean genagelte_Linien=false;// laufend6.6.2014
		double kleiner=1;//Es ist eine zu machende Sache
		int Sorte_von_Schnittstelle=Interface2.Links; 
		int ins_Rad=4;
		boolean Rad=(ins_Rad!=0); 
		for(int jj=0;jj<Zahl_von_der_BM;jj++)
			BM[jj]=new topologischeBoltzmannmaschine((waagerechte_Linie? (Reihen-ins_Rad):0)+
														(genagelte_Linie? (Reihen-1):0)+
														(genagelte_Linien?(Reihen-1):0)+
														ins_Rad+
														(Reihen-1),
														Reihen);
		EBM=BM[0];
		if(!nurEBM)  
			MBM=BM[1];    
		else 
			 MBM=EBM;  
		Pottsnagel.Anfang();
		for(int bm=0;bm<(nurEBM?1:2);bm++)
			{ 
			Pottsnagel Potts1=new Pottsnagel(); 
			Pottsnagel Potts2=new Pottsnagel();  
			int Nummer_von_Beziehung,dieGruppe;
			int i; 
			Stelle[] variables=BM[bm].variables; 
			Beziehung[] Beziehungen=BM[bm].Beziehungen;
			for(Nummer_von_Beziehung=0,i=0,dieGruppe=0;dieGruppe<EBM.Reihe();i+=4,dieGruppe++)
				{
				if(waagerechte_Linie)
					if((dieGruppe+ins_Rad<EBM.Reihe()))     // mit den unteren Sachen verknüpft            
						Beziehungen[Nummer_von_Beziehung++]=new Lineal (0.03*laufend6/*laufend6.6.2014*/*alles_ist_null,new Stelle[]{ 
							variables[i+0],variables[i+1],variables[i+2],variables[i+3]}	);
				if(dieGruppe+1<EBM.Reihe())//nicht die Letzte
					{
						if(genagelte_Linie)
						{ 
/*bm=0
 * Wir sind 1
Wir sind 2
Zahl_von_Federn 0
Zahl_von_Federn 1
Zahl_von_Federn 2
Zahl_von_Federn 3
Zahl_von_Federn 4
Zahl_von_Federn 5
Zahl_von_Federn 6
Zahl_von_Federn 7
Zahl_von_Federn 8
Zahl_von_Federn 9
Zahl_von_Federn 10
Zahl_von_Federn 11
Zahl_von_Federn 12
Zahl_von_Federn 13
Zahl_von_Federn 14
Zahl_von_Federn 15
Zahl_von_Federn 16
Zahl_von_Federn 17
Zahl_von_Federn 18
bm=1
potts:               Wir sind 3
Wir sind 4
Zahl_von_Federn 0
Zahl_von_Federn 1
Zahl_von_Federn 2
Zahl_von_Federn 3
Zahl_von_Federn 4
Zahl_von_Federn 5
Zahl_von_Federn 6
Zahl_von_Federn 7
Zahl_von_Federn 8
Zahl_von_Federn 9
Zahl_von_Federn 10
Zahl_von_Federn 11
Zahl_von_Federn 12
Zahl_von_Federn 13
Zahl_von_Federn 14
Zahl_von_Federn 15
Zahl_von_Federn 16
Zahl_von_Federn 17
Zahl_von_Federn 18
*/
							Beziehungen[Nummer_von_Beziehung++]=Potts1.hinzufuegen(new genagelte_Feder(3,new Stelle[]{ 
													variables[4+i+2],variables[4+i+3]},
													dieGruppe*topologischeBoltzmannmaschine.Abstand_zw(),
													(200-110+30+Math.sin(dieGruppe)*11)/kleiner)); 
						}
																	
						if(genagelte_Linien)
						{ 
							Beziehungen[Nummer_von_Beziehung++]=Potts2.hinzufuegen(new genagelte_Feder(3, new Stelle[]{ 
													variables[4+i+2],variables[4+i+3]},
													dieGruppe*topologischeBoltzmannmaschine.Abstand_zw(),
													(200-110-10-Math.sin(dieGruppe)*11)/kleiner)); 
							 
						} 
					Beziehungen[Nummer_von_Beziehung++]=new senkrechte_Feder(0.01*laufend6/*laufend6.6.2014*/*alles_ist_null,new Stelle[]{ 
							variables[0+i+2],variables[0+i+3],variables[4+i+2],variables[4+i+3]}	); 
					if(!(dieGruppe+ins_Rad+1<EBM.Reihe())) //Räder
						Beziehungen[Nummer_von_Beziehung++]=new Quadratfeder(((alles_ist_null*Sorte_von_Schnittstelle==Interface2.Fuers_Rad)?10:1)
																		*0.0000001/(Math.pow(ins_Rad,1.5)),
																new Stelle[]{ 
								variables[0+i+2],variables[0+i+3],variables[4+i+2],variables[4+i+3]}	);
					}
				}  
			} 
		if(!nurEBM) 
			{ 
			boolean choice=true;
			Beziehung b1=null;
			if(!choice)
				b1=new ausgerichtete_Feder(0.02,new Stelle[]{EBM.variables[6],EBM.variables[7],MBM.variables[6],MBM.variables[7]});
		 	Beziehung b2=new ausgerichtete_Feder(0.8,new Stelle[]{EBM.variables[2],EBM.variables[3],MBM.variables[2],MBM.variables[3]});
			if(choice )
				Schnittstelle0=new Interface(EBM,MBM,1,new Beziehung[]{ b2 });
			else
				Schnittstelle0=new Interface(EBM,MBM,2,new Beziehung[]{ b1,b2});	 
			Schnittstelle=new Interface2(BM,Zahl_von_der_BM,Sorte_von_Schnittstelle); 
			} 
		kleinen_Fehler(1); 
		}
	}
		
	static boolean ptrFertig;
	
	public void setSomeParameters(int nParameters, double[] in_parameters) {   
            force=in_parameters[1];
            //q=parameters[2];
            //c=parameters[3];
            //g=parameters[4];
            //d=parameters[5];
            //v=parameters[6];
            //gamma=parameters[7];  
	        //g1=parameters[8]; 
			//linkeUnruhe=parameters[9]; 
			//rechteUnruhe=parameters[10]; 
			//Einfluss=parameters[11];  	 	
    }
	
	private static boolean _initValues_wurde_gemacht;
	boolean initValues_wurde_gemacht(){
		if(!_initValues_wurde_gemacht){
			_initValues_wurde_gemacht=true;
			return false;
		}
		return _initValues_wurde_gemacht;
	}
	
	public void rechnen(){ 
//	synchronized(rbn.pds){
		if(!initValues_wurde_gemacht()) 
			initValues();//verursacht 464864875454868768786   ?
		
		
		if(SCHWARZWPRUEFUNG&&UEBERPRUEFUNG_VON_NN){
		
		
				
		{//0. Stock  NEHMEN 
		for(int v=0;v<NumberOfLpn;v++) {  
				Be_lpn derLPN=be_lpn[v];
				if(derLPN.zStelleVonKoerper==0)
					derLPN.Wert=affine_f(Haut.pds[0][derLPN.xStelleVonKoerper][derLPN.yStelleVonKoerper]);
			} 
		}
		 
		{//1. Stock be RECHNEN
		for(int v=0;v<NumberOfLpn;v++) { 
			Be_lpn derLPN=be_lpn[v];
			if(derLPN.zStelleVonKoerper==1){
				double Summe=derLPN.affinerBeiwert;
				for(short c=0;c<PRESY_NB;c++){ 					
					double linearer=affine_f(0);
					if(Ja_oder_Nein( derLPN.xZweigstelle[c] ,
									 derLPN.yZweigstelle[c],
						derLPN.zZweigstelle))
						linearer=affine_f(1);
					Summe+=linearer*derLPN.Staerke[c];
				}
				if(AFFINE)
					derLPN.Wert=Summe;
				else{
					derLPN.Wert=Summe-.5; 
					if(v==7)
						derLPN.Wert=(Summe+.5)/2+.25;
				}
			}
		}
		}

		{//  HINSTELLEN be  (überall jetzt : 1. Stock
		for(int v=0;v<NumberOfLpn;v++) {  
				Be_lpn derLPN=be_lpn[v]; 
				if(derLPN.zStelleVonKoerper>=1)
					Haut.pds[0][derLPN.xStelleVonKoerper][derLPN.yStelleVonKoerper]=affine_b(derLPN.Wert);
		} 
		}
		
	  	{ // ki
		{// ki RECHNEN
		for(int v=0;v<NumberOfLpn;v++) { 
			Ki_lpn derLPN=ki_lpn[v]; 
			{
				double Summe=derLPN.affinerBeiwert;
				for(short x=0;x<Versuchbreite;x++)
					for(short y=0;y<Versuchbreite;y++){
						Summe+=affine_f(Haut.pds[0][derLPN.xZweigstelle[x+Versuchbreite*y]][
										 derLPN.yZweigstelle[x+Versuchbreite*y]]) 
							*derLPN.Staerke[x+Versuchbreite*y];
					}
				if(AFFINE)
					derLPN.Wert=Summe;
				else{
					derLPN.Wert=Summe-.5; 
					if(v==7)
						derLPN.Wert=(Summe+.5)/2+.25;
					}
			}
		}
		}	
		{//    HINSTELLEN ki 2.Stock 
		int verschoben;
		if(!tanulj)  
			verschoben=8;
		else 
			verschoben=0;
		for(int v=0;v<NumberOfLpn;v++) {  
				Ki_lpn derLPN=ki_lpn[v]; 
				if(derLPN.zStelleVonKoerper==2)
					Haut.pds[0][derLPN.xStelleVonKoerper+verschoben][derLPN.yStelleVonKoerper]=
								affine_b(derLPN.Wert);
		} 
		}
		}
	
 
		
		for(int s=0;s<SternzellZahl;s++){ //Sternzellen
			Sternzelle dieSz=sz[s];
			dieSz.rechnen();
		}
			 
			
		lernen();
		}
	  
	 
	 //////rbn:    RBN wird gerechnet
	 if(SCHWARZWPRUEFUNG){ 
	 if(!UEBERPRUEFUNG_VON_NN){ 

		short nachbarn=1; 
		for(short x=0;x<RBNGROESSE;x++)
		for(short y=0;y<RBNGROESSE;y++){
			byte alive=0; 
			for(int i=-nachbarn;i<=nachbarn;i++)
			for(int j=-nachbarn;j<=nachbarn;j++){
				short x_von_nachbarn,y_von_nachbarn;
				x_von_nachbarn=(short)(int)(((x+i)+RBNGROESSE)%RBNGROESSE);
				y_von_nachbarn=(short)(int)(((y+j)+RBNGROESSE)%RBNGROESSE);
				if(rbn.pds[0][ x_von_nachbarn][y_von_nachbarn ]>.5  )
					alive++;
			}
			switch(0){
				case 0://Conway
				if(rbn.pds[0][ x ][y ]>.5 ){
						alive--;
						zukuenftige[x][y]=(alive==2)||(alive==3);
						}
					else zukuenftige[x][y]=(alive==3);  
				break;
				case 1:// Kopierwelt 
				if(rbn.pds[0][ x ][y ]>.5 ) 
						alive--; 				
					zukuenftige[x][y]=(alive==1)||(alive==3)||(alive==5)||(alive==7); 
				break; 
				case 2://Linien 
				if(rbn.pds[0][ x ][y ]>.5 ){
						alive--;
						zukuenftige[x][y]= (alive==0)||(alive==1)||(alive==4)||(alive==5)||(alive==7) ;
						} 
				else	 
						zukuenftige[x][y]= (alive==1)||(alive==3)||(alive==4)||(alive==5)||(alive==8) ;
				break;
			}
		}				
						
		for(int x=0;x<RBNGROESSE;x++)
		for(int y=0;y<RBNGROESSE;y++){
			 rbn.pds[0][x][y]=(1-force)* rbn.pds[0][x][y]
								+force*(zukuenftige[x][y]?1:0);
		}
	} 
	
	
	druecken();
			
	}
} 
	private void druecken(){
	if(!Entspannung)
		return;   
if(false)//laufend4784864684864684684684687
	Interface2.druecken(Schnittstelle); 
else  
	Interface.druecken(Schnittstelle0); 

	}
	
	public boolean Ja_oder_Nein(short x,short y,short z){
		if(z==0)
			return Haut.Ja_oder_Nein(x,y);
		else{
			 throw new AssertionError("Bitte hinzufuegt! z==0)  Haut.Ja_oder_Nein(x,y)");  
		}
	}
	 
	public void  lernen (){
	
		if(BENUTZT_Sternzellen)
		for(int z=0;z<SternzellZahl;z++){
			Be_lpn derLPN=be_lpn[z]; 
			Sternzelle dosz=sz[z];
			//0.1*
		}	
		
		if(tanulj){
		double eta=0.03;
		{ //the be(1) and the ki (2) are compared
			
		int ski=2; 
		short bki=(short)(int)(ski*Versuchbreite*Versuchbreite);
		int sbe=1; 
		short bbe=(short)(int)(sbe*Versuchbreite*Versuchbreite);
		for(short x=0;x<Versuchbreite;x++)
			for(short y=0;y<Versuchbreite;y++)	{
				Be_lpn be=be_lpn[bbe+NummerDerLpn(x,y)];
				Ki_lpn ki=ki_lpn[bki+NummerDerLpn(x,y)];  
				if(!(be.yStelleVonKoerper+ Versuchbreite*Versuchbreite==ki.yStelleVonKoerper)) 
					throw new AssertionError("hey!");
				double diff=ki.Wert-be.Wert;
				for(short xx=0;xx<Versuchbreite;xx++)
					for(short yy=0;yy<Versuchbreite;yy++){ 
						short xZweigstelle=be.xZweigstelle[xx+Versuchbreite*yy];
						short yZweigstelle=be.yZweigstelle[xx+Versuchbreite*yy];
						be.Staerke[xx+Versuchbreite*yy]+=
							eta*diff*affine_f(Haut.pds[0][xZweigstelle][yZweigstelle]);
				}
				be.affinerBeiwert+=
							eta*diff;
		}
		}
		}

		
	}
	
	public void  zeichnen (){
	   
		if(!initValues_wurde_gemacht()) 
			initValues(); 
		if(false)
		{
		 
			System.out.print ("        ----->    "   );
		{ 
		for (int a=0;a< NumberOfLpn;a++) 
			System.out.print ( (Math.round( (float)(be_lpn[a]. Wert *9))* 1)+" "); 
		System.out.print ( "    ");
	/* 	for (int a=0;a< SternzellZahl;a++) 
			System.out.print ( (Math.round( (float)(0.5* sz[a]. Wert *9))* 1)+" "); 
		System.out.println ( );
	 */	System.out.flush( ); 
		}
		System.out.flush( ); 
	}
	}

	int NummerDerLpn(int x,int y){
		return (x-20*00000000000000)+ Versuchbreite*y;
	}

	public static double hyst(double x, double threshold, double beta){
		return 1/(1+Math.exp(beta*(threshold-x)));
	}
	
	public static double affine_f(double x){ 
		if(AFFINE)
			return x;
		else
			return 2*x-1;
	}
	public static double affine_b(double x){ 
		if(AFFINE)
			return x;
		else
			return .5*(x+1);
	}

	
}


class Variable{
	double Kommazahl;
	public Variable(){
		Kommazahl= 0;
	}
}


class Variable_mit_Zwischenspeicher extends Variable{
	protected Variable Speicher;
	public Variable_mit_Zwischenspeicher(){
		Speicher=new Variable();
	}
}

class Stelle extends Variable_mit_Zwischenspeicher{ 
	public 	double v;
	public  double a;
	
	public double gibt_a(){
		return 	gibt_a(true);
	}
	public double gibt_a(boolean Galileo){
	if(Galileo)
		return a;
	else
		return super.Speicher.Kommazahl;
	}
	public void setzt_a_fest(double x){
		setzt_a_fest(x,true);
	}
	public void setzt_a_fest(double x,boolean Galileo){
	if(Galileo)
		a=x;
	else
		super.Speicher.Kommazahl=x;
	}
	public void hinzufuegt(double x){
		hinzufuegt(x,true);
	}
	public void hinzufuegt(double x,boolean Galileo){
	if(Galileo) 
		a+=x;
	else
		super.Speicher.Kommazahl+=x; 
	}
	public Stelle(){
		super();
		v=0;
		a=0;
	} 
	private Variable Speicher;//um Ihn zu verstecken
}

abstract class Beziehung{

	protected Stelle[] variables;
	final int variableNb;
	public static boolean xdotdot;

public Beziehung(int v,Stelle[] array){
	variableNb=v;
	variables=new Stelle[variableNb];
	for (int i=0;i<variableNb;i++)
		variables[i]=array[i];
	}
		
public abstract void druecken(boolean Galileo);
public void druecken(){
	druecken(NN.Galileo);
}
public void Ruhe()
	{
	}
	
}

abstract class zweipunktigeBeziehung extends Beziehung
	{ 
	public zweipunktigeBeziehung(Stelle[] array)
		{
		super(4,array);
		}
	}
	
class Bumper extends zweipunktigeBeziehung{ 
	public double Härte;  
	public Bumper(Stelle[] array){
		this(20,array);
	} 
	public Bumper(double Härte,Stelle[] array)
		{
		super(array);
		this.Härte=Härte; 
		} 
	public void druecken(boolean Galileo)
	{ 
	double Spannung=(variables[1].Kommazahl-variables[3].Kommazahl); 
	Spannung*=Spannung;
	Spannung*=Spannung;
	{
	double ys=(variables[0].Kommazahl-variables[2].Kommazahl); 
	Spannung+=(ys*ys)*(ys*ys);
	}
	variables[3].hinzufuegt(-Härte*exp(-0.00000085*Spannung),Galileo);//die Hälfte für 30 pixel.
	//if(Spannung<0) Spannung=0;
	//variables[3].hinzufuegt(-Härte*exp(-0.1*Spannung),Galileo);   
	}
}


class kuerze_Feder extends zweipunktigeBeziehung{
	double Steifheit;
	
public kuerze_Feder(Stelle[] array){
	this(0.01,array); 
	}
public kuerze_Feder(double dieSteifheit,Stelle[] array){
	super(array);
	Steifheit=dieSteifheit;
	} 
public void druecken(boolean Galileo)
	{  
	double Spannung=variables[1].Kommazahl-variables[3].Kommazahl; 
	double change=100* 
		 (Spannung)*Steifheit; 
	variables[3].hinzufuegt(+change,Galileo); 
	variables[1].hinzufuegt(-change,Galileo); 
		 
	Spannung=variables[0].Kommazahl-variables[2].Kommazahl;  
	change=100* 
		 (Spannung)*Steifheit;  
	variables[2].hinzufuegt(+change,Galileo); 
	variables[0].hinzufuegt(-change,Galileo); 
	} 	
	
}


class Feder extends kuerze_Feder{
	double Länge;
	 
public Feder(double dieSteifheit,Stelle[] array){
	super(dieSteifheit,array);
	Länge=10;
	Ruhe();
	} 
public void Ruhe()
	{
	variables[2].Kommazahl=variables[0].Kommazahl+Länge*Math.cos(+0.3); 
	variables[3].Kommazahl=variables[1].Kommazahl+Länge*Math.sin(+0.3); 
	}
public void druecken(boolean Galileo)
	{  
	double ySpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[1].Kommazahl-variables[3].Kommazahl); 
	double xSpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[0].Kommazahl-variables[2].Kommazahl);  
	double Spannung=sqrt(ySpannung*ySpannung+xSpannung*xSpannung);
	if(Spannung==0)
		return; 
	ySpannung/=Spannung;
	xSpannung/=Spannung;
	Spannung=Spannung-Länge;
	Spannung*=Spannung;
	ySpannung*=Spannung;
	xSpannung*=Spannung;
	
	double change=100* 
		 (ySpannung)*Steifheit; 
	variables[3].hinzufuegt(+change,Galileo);  
	if(true){//if(die Feder nicht ausgerichtet sein muß)  
		variables[1].hinzufuegt(-change,Galileo); 
		}
		
	change=100* 
		 (xSpannung)*Steifheit;  
	variables[2].hinzufuegt(+change,Galileo);
	if(true){//if(die Feder nicht ausgerichtet sein muß) 
		variables[0].hinzufuegt(-change,Galileo);  
		}
	} 	
	
}





class Quadratfeder extends kuerze_Feder{//V=(x²-y²-10)²
	double Länge;
	 
public Quadratfeder(double dieSteifheit,Stelle[] array){
	super(dieSteifheit,array);
	Länge=5;
	Ruhe();
	} 
public void Ruhe()
	{
	variables[2].Kommazahl=variables[0].Kommazahl+Länge*Math.cos(+0.3); 
	variables[3].Kommazahl=variables[1].Kommazahl+Länge*Math.sin(+0.3); 
	}
public void druecken(boolean Galileo)
	{  
	double EntfernungX=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[0].Kommazahl-variables[2].Kommazahl);  
	double EntfernungY=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[1].Kommazahl-variables[3].Kommazahl); 
	double Spannung=(EntfernungY*EntfernungY+EntfernungX*EntfernungX); 
	double RichtungX,RichtungY;
	{
		double s=sqrt(Spannung);
		RichtungX=EntfernungX/s;
		RichtungY=EntfernungY/s; 
	}
	Spannung=Spannung-Länge*Länge;
	
	double change=Steifheit*Spannung; 
	EntfernungX*=change; 
	EntfernungY*=change; 
	
	variables[2].hinzufuegt(+EntfernungX,Galileo);  
	variables[3].hinzufuegt(+EntfernungY,Galileo); 
	if(true){//if(die Feder nicht ausgerichtet sein muß) //DANN SIND SIE ZU WEIT WEG!
		variables[0].hinzufuegt(-EntfernungX,Galileo); 
		variables[1].hinzufuegt(-EntfernungY,Galileo); 
		}
	} 	
	
}


class senkrechte_ausgerichtete_lange_Feder extends zweipunktigeBeziehung{
	double Steifheit;
	
public senkrechte_ausgerichtete_lange_Feder(Stelle[] array){
	this(0.01,array); 
	}
public senkrechte_ausgerichtete_lange_Feder(double dieSteifheit,Stelle[] array){
	super(array);
	Steifheit=dieSteifheit;
	}
protected double Spannung(){
	//double kleiner=256/Breite();   //Verbesserung184684 :  110/kleiner   
	return variables[1].Kommazahl-variables[3].Kommazahl -110; 
	}
public void druecken(boolean Galileo)
	{  
	double change=100*
		 (Spannung())*Steifheit;   
	variables[3].hinzufuegt(change,Galileo);  
	}
	 
}
 
class Lineal extends senkrechte_ausgerichtete_lange_Feder{ 
	 
public Lineal(double dieSteifheit_hoch_Null_komma_3,Stelle[] array){
	super(dieSteifheit_hoch_Null_komma_3*dieSteifheit_hoch_Null_komma_3*dieSteifheit_hoch_Null_komma_3
			,array); 
	}
public void druecken(boolean Galileo)
	{ 
	double Spannung=variables[1].Kommazahl-variables[3].Kommazahl;   
	variables[3].hinzufuegt(Steifheit*Math.pow(Spannung(),3),Galileo);  
	}
	 
}

class senkrechte_Feder extends zweipunktigeBeziehung{
	double Steifheit;
	
public senkrechte_Feder(Stelle[] array){
	this(0.01,array); 
	}
public senkrechte_Feder(double dieSteifheit,Stelle[] array){
	super(array);
	Steifheit=dieSteifheit;
	}
public void druecken(boolean Galileo)
	{ 
	double Spannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[1].Kommazahl-variables[3].Kommazahl); 
	double change=100*
		 (Spannung)*Steifheit;   
	variables[3].hinzufuegt(+change,Galileo);  
	variables[1].hinzufuegt(-change,Galileo);  
	}
	 
}

class waagerechte_ausgerichtete_Feder extends zweipunktigeBeziehung{
	double Steifheit;
	
public waagerechte_ausgerichtete_Feder(Stelle[] array){
	this(0.01,array); 
	}
public waagerechte_ausgerichtete_Feder(double dieSteifheit,Stelle[] array){
	super(array);
	Steifheit=dieSteifheit;
	}
public void druecken(boolean Galileo)
	{  
	double Spannung=variables[0].Kommazahl-variables[2].Kommazahl; 
	double change=100*
		 (Spannung)*Steifheit;   
	variables[2].hinzufuegt( change,Galileo);  
	}
	 
}


class ausgerichtete_Feder extends zweipunktigeBeziehung{
	double Steifheit;
	
public ausgerichtete_Feder(Stelle[] array){
	this(0.01,array); 
	}
public ausgerichtete_Feder(double dieSteifheit,Stelle[] array){
	super(array);
	Steifheit=dieSteifheit;
	}
public void druecken(boolean Galileo)
	{ 
	double Spannung=variables[3].Kommazahl-variables[1].Kommazahl; 
	double change=100* 
		 (Spannung)*Steifheit;  
	variables[3].hinzufuegt(-change,Galileo); 
		 
	Spannung=variables[2].Kommazahl-variables[0].Kommazahl;  
	change=100* 
		 (Spannung)*Steifheit;  
	variables[2].hinzufuegt(-change,Galileo); 
	}
	 
}


class Ueberraschung extends Beziehung{
public Ueberraschung(int v,Stelle[] array){
	super(v,array);
	}
public void druecken(boolean Galileo)
	{ 
	double Verschiebung=Math.random()*0.002;
	for (int i=0;i<variableNb;i++)
		variables[i*2].hinzufuegt(Verschiebung+.011*Math.sin(Zellnetz.t/5.));
	}
	
}


//  http://www.arndt-bruenner.de/mathe/java/plotter.htm
final class Mathematik
{ 
static double FederDruck(double x)
	{
	double y=1+0.01*x*x;
	return x/(1+sqrt(y));
	}

 

static double DruckPotentialKraftadfadfasdfasdfasfd(double x)  
	{ 
	return 100*exp(-0.3*x);
	}  

static double FederDruckKraft4545(double x)
	{
	if(Math.abs(x)<0.0000001)
		x=0.0000001*x/Math.abs(x); 
	return 1000*Math.pow(x,-3);
	}  

}
 
class genagelte_Feder extends Beziehung{    //bitte sei innere Klasse von Pottsnagel
		double ZentrumX,ZentrumY;
		public  double Steifheit;
		public  double Treibung;
		private double minus_Steifheit_hoch_zwei; 
		private Pottsnagel Potts;  
	 
public genagelte_Feder(Stelle[] array,double x,double y){
	this(10,array,x,y); 
			//	http://de.wikipedia.org/wiki/aperiodischer_Grenzfall  
			// Steifheit>=13 : Schwingung durch falscher Diskretizierung. Danach platzt es.
			// Steifheit=12   : Es hat ´ne sehr gute Diskretizierung.  
			// Steifheit=0.03 : Es hat ´ne sehr gute Diskretizierung und ist langsam.
			// Steifheit<0  : Es platzt.
	}
public genagelte_Feder(double Steifheit,Stelle[] array,double x,double y){
	this(Steifheit,1,array,x,y); 			
			//  ohne Diskretizierung/theoritisch : 0<=Steifheit<=1  => immer kleiner
			//	Treibung=1 => den aperiodischen Grenzfall . Treibung=0=>sin. Treibung=0.1=>einige Schwingungen
			//  Treibung>>0.01 => alles zu klingt schnell ab
}
public genagelte_Feder(double Steifheit,double Treibung,Stelle[] array,double x,double y){
	super(2,array); 
	Potts=null;
	this.Steifheit=Steifheit;
	this.Treibung=Treibung;
	minus_Steifheit_hoch_zwei=-this.Steifheit*this.Steifheit;
	ZentrumX=x;
	ZentrumY=y; 
	if(false){//"false" weil es sowieso zerquetscht werden wird
		Ruhe(); 
		if(false){
			variables[0].Kommazahl=ZentrumX; 
			variables[1].Kommazahl=ZentrumY+10; 
			variables[0].v=0;
			variables[1].v=0; 
		}
	}
	} 
public void Ruhe()
	{
		if(true){
	variables[0].Kommazahl=ZentrumX; 
	variables[1].Kommazahl=ZentrumY+10; 
		}
	variables[0].v=0;
	variables[1].v=0; 
	}
public void druecken(boolean Galileo)//Galileo wird nicht benutzt
	{  
	if(!Habe_ich_einen_Pottsnagel()){  
		if(true)//if(es eine innere Klasse von Pottsnagel ist)
			druecken_fuer_PN(Galileo);
		else
			throw new AssertionError("?!?!?");
		return;
	}
	mein_Pottsnagel().druecken_wenn_nicht_schon_gemacht();
	} 
public void druecken_fuer_PN(boolean Galileo)//Galileo wird nicht benutzt
	{   
	double xSpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[0].Kommazahl-ZentrumX);  
	double ySpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[1].Kommazahl-ZentrumY);  
	double T=-2*Treibung*Steifheit;//um das Rechnen zu beschleunigen
	variables[0].hinzufuegt(T*variables[0].v+minus_Steifheit_hoch_zwei*xSpannung,true);  
	variables[1].hinzufuegt(T*variables[1].v+minus_Steifheit_hoch_zwei*ySpannung,true);  
	}
public double passt()//Gasdökasjölashdlahsdlkhasdhalileo wird nicht benutzt
	{   
	double xSpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[0].Kommazahl-ZentrumX);  
	double ySpannung=topologischeBoltzmannmaschine.ausgerichtete_Entfernung(
						variables[1].Kommazahl-ZentrumY); 
	return xSpannung*xSpannung+ySpannung*ySpannung; 
	}
public boolean Habe_ich_einen_Pottsnagel()
	{
	return !(Potts==null);
	}
public Pottsnagel mein_Pottsnagel()
	{
	if(!Habe_ich_einen_Pottsnagel())
		throw new AssertionError("if ...");
	return Potts;
	}
public void mein_Pottsnagel(Pottsnagel P)
	{
	if(Potts!=null)
		if(P!=Potts)
			throw new AssertionError();
	Potts=P;
	}
}

class Pottsnagel{
	private genagelte_Feder[] die_genagelte_Federn;
	private int Zahl_von_Federn;  
	private int Wie_viel;
	private final int MAX=20;
	public double in;
	public double erste_Steifheit;
	public double Wert;
	public double compute_in()
	{
		double Ergebnis=0;   
		for(int N=0;N<Zahl_von_Federn;N++)
			Ergebnis+=die_genagelte_Federn[N].passt()  ;
		in=Ergebnis;
		if(false)System.out.println(in);
		return in;
	}
	public void press()
	{
		if(compute_in()<10*10*30)
			set_Stei(erste_Steifheit*Wert);
 else
			set_Stei(erste_Steifheit);
	}
	public void set_Stei(double Steifheit)
	{
		for(int N=0;N<Zahl_von_Federn;N++)
			die_genagelte_Federn[N].Steifheit=Steifheit;
	}
	public Pottsnagel(){
		Zahl_von_Federn=0;
		die_genagelte_Federn=new genagelte_Feder[MAX]; 
		Wie_viel=0;
		Wert=0.1;
		if(wir_sind==0)
			uns=new Pottsnagel[40]; 
		if(wir_sind==40)
			throw new AssertionError();
		uns[wir_sind++]=this; 
		if(false)System.out.println("Wir sind "+wir_sind); 
		System.out.flush(); 
		}
	public genagelte_Feder hinzufuegen(genagelte_Feder F)
		{
		F.mein_Pottsnagel(this);
		if(false)System.out.println("Zahl_von_Federn "+Zahl_von_Federn );
		if(Zahl_von_Federn+1>MAX)
			System.out.println("Bitte: sei Pottsnagel.MAX nicht < "+Zahl_von_Federn+1);
		System.out.flush(); 
		die_genagelte_Federn[Zahl_von_Federn++]=F;
		erste_Steifheit=F.Steifheit;
		return F;
		}
	public void druecken_wenn_nicht_schon_gemacht()
		{   
		boolean Ich_habe_schon_gedrueckt=!(Wie_viel==0);
		Wie_viel++;
		if(!Ich_habe_schon_gedrueckt)
			druecken();
		if(Wie_viel>=Zahl_von_Federn)
			Wie_viel=0;
		}
	private void druecken()
		{     
		for(int N=0;N<Zahl_von_Federn;N++)
			die_genagelte_Federn[N].druecken_fuer_PN(false);
		}  
		
	public static Pottsnagel[] uns;
	public static int wir_sind; 
	public static void Anfang()
	{
		wir_sind=0;
		uns=null;
	}
	public static void pressAll()
	{
	if(wir_sind==0)
		return;
	for(int u=0;u<wir_sind;u++)
		uns[u].press();
	}
}

class Interface extends BoltzmannmaschineOderInterface 
{
	Boltzmannmaschine EBM,MBM;
	Variable[] Beruehrten;
	int variableNb;
	
	public Interface(Boltzmannmaschine EBM,Boltzmannmaschine MBM,
			int Zahl_Von_Beziehungen,Beziehung[] Beziehungen)
		{
		super(Zahl_Von_Beziehungen,Beziehungen);
		if((EBM==null)||(MBM==null))
				throw new AssertionError("Scheiße!");
		this.EBM=EBM;
		this.MBM=MBM;
		int MAX=123;
		Beruehrten=new Variable[MAX];
		variableNb=0;
		for(int a=0;a<Zahl_Von_Beziehungen;a++)
			{
			for(int b=0;b<this.Beziehungen[a].variableNb;b++)
				{
				Variable v=this.Beziehungen[a].variables[b];
				if(!ist_eine_meiner_Var(v)){ 
					if(variableNb==MAX)
						throw new AssertionError("MAX ist zu klein, bitte.");
					Beruehrten[variableNb++]=v;
					}
				}
			}			
		}
		
	public boolean ist_eine_meiner_Var(Variable v)
	{ 
		for(int c=0;c<variableNb;c++)
			if(v==Beruehrten[c])
				return true;
		return false;
	}
	
	public void druecken(){
		super.druecken(false);
	}
	public void druecken(boolean a){
		throw new AssertionError("I do not like :\"druecken(true)\".");
	}
	
	public static void druecken(Interface Schnittstelle)
		{
		Boltzmannmaschine[] Maschine;
		Maschine=new Boltzmannmaschine[2];
		Maschine[0]=Schnittstelle.EBM;
		Maschine[1]=Schnittstelle.MBM;
		druecken(Schnittstelle,Maschine,2);
		}
	public static void druecken(Interface Schnittstelle,Boltzmannmaschine[] alleMaschinen,int Zahl_von_der_BM)
		{
		druecken_(Schnittstelle,alleMaschinen,Zahl_von_der_BM);
		} 
	private static void druecken_(Interface Schnittstelle,Boltzmannmaschine[] alleMaschinen,int Zahl_von_der_BM)
		{   
		if(Schnittstelle!=null) 
			{
			if(Zahl_von_der_BM<2)
				throw new AssertionError("Scheiße!: Zahl_von_der_BM ist kleiner als 2...");
			if((alleMaschinen==null) )
				throw new AssertionError("Scheiße!"); 
			if((alleMaschinen[0]!=Schnittstelle.EBM)||(alleMaschinen[1]!=Schnittstelle.MBM))
				throw new AssertionError("Der Programmer hat einen Fehler gemacht.");
			} 
		else
			if(!(Zahl_von_der_BM>0))
				throw new AssertionError("Es gibt keine Maschinen!");
		Boltzmannmaschine[] Maschine;
		Maschine=new Boltzmannmaschine[Zahl_von_der_BM];
		for(int a=0;a<Zahl_von_der_BM;a++)
			Maschine[a]=alleMaschinen[a]; 
		for(int bm=0;bm<Zahl_von_der_BM;bm++)
			for(int i=0;i<Maschine[bm].number_of_variables;i++)
				{
				Maschine[bm].variables[i].setzt_a_fest(0); 
				Maschine[bm].variables[i].setzt_a_fest(0,false); 
				}   
			if(NN.uebereinstimmen)
				if(!( nurEBM ))
					Schnittstelle.druecken();   
		boolean zusammen=true;
			if(!zusammen){
				Maschine[0].druecken(NN.Galileo);
		for(int bm=1;bm<Zahl_von_der_BM;bm++)
			for(int u=0;u<4;u++)
				{
				Maschine[bm].variables[u].setzt_a_fest( Maschine[0].variables[u].gibt_a());  
				Maschine[bm].variables[u].v= 		     Maschine[0].variables[u].v;  
				Maschine[bm].variables[u].Kommazahl=    Maschine[0].variables[u].Kommazahl;  
				} 
		for(int bm=1;bm<Zahl_von_der_BM;bm++)
			Maschine[bm].druecken(NN.Galileo);
			}
else	
		for(int bm=0;bm<Zahl_von_der_BM;bm++)
			Maschine[bm].druecken(NN.Galileo);
				
		Pottsnagel.pressAll();
		for(int bm=0;bm<Zahl_von_der_BM;bm++)
				{ 
				boolean nach_Galileo=true;
				if(true)//if( (NN.Galileo&&!(Schnittstelle0.ist_eine_meiner_Var(vari)   
					//												)))
					Maschine[bm].setzt_neue_Stelle(nach_Galileo,Schnittstelle,bm==1);				 
				} 	 
		}
}

abstract class BoltzmannmaschineOderInterface
{
	public Beziehung Beziehungen[];
	public int Zahl_Von_Beziehungen;
	
	public BoltzmannmaschineOderInterface(int Zahl_Von_Beziehungen)
	{
	Beziehungen=new Beziehung[this.Zahl_Von_Beziehungen=Zahl_Von_Beziehungen];  
	}
	
	public BoltzmannmaschineOderInterface(int Zahl_Von_Beziehungen,Beziehung[] Beziehungen)
	{
	this(Zahl_Von_Beziehungen);
	for(int a=0;a<Zahl_Von_Beziehungen;a++)
		this.Beziehungen[a]=Beziehungen[a]; 
	this.Zahl_Von_Beziehungen=Zahl_Von_Beziehungen; 
	}		 
	protected void druecken(boolean Galileo)
	{     		int i=-1;	try{ 
			for(i=0;i<Zahl_Von_Beziehungen;i++)  
				Beziehungen[i].druecken(Galileo);   }catch(java.lang.NullPointerException e)
				{System.out.println(i);}
	} 
		
}

class Boltzmannmaschine extends BoltzmannmaschineOderInterface 
{
	public final Stelle[] variables;
	int number_of_variables;
	 	
	
	public Boltzmannmaschine(int Zahl_Von_Beziehungen,int number_of_variables)
		{  
		super(Zahl_Von_Beziehungen);
		this.variables=new Stelle[number_of_variables]; 
		for(int z=0;z<number_of_variables;z++)
			variables[z]=new Stelle();  
		this.number_of_variables=number_of_variables;
		}
	public Boltzmannmaschine(int Zahl_Von_Beziehungen,Stelle[] variables)
		{
		super(Zahl_Von_Beziehungen);
		this.variables=variables;
		}
	public void setzt_neue_Stelle(){
		for(int i=0;i<number_of_variables;i++)
			{
			Stelle vari=variables[i];
			vari.v+=Zellnetz.get_dt()*vari.gibt_a();  //die Geschwindigkeiten rechnen
			vari.Kommazahl+=Zellnetz.get_dt()*vari.v; 			//die Stellen rechnen
			} 		 
	}
	public void setzt_neue_Stelle(boolean Galileo,Interface Schnittstelle0,boolean ist_MBM)
		{
		for(int i=0;i<number_of_variables;i++)
			{
			Stelle vari=variables[i];
			vari.v+=Zellnetz.get_dt()*vari.gibt_a();  //die Geschwindigkeiten rechnen
			vari.Kommazahl+=Zellnetz.get_dt()*vari.v; 			//die Stellen rechnen
			
			boolean nach_Galileo=NN.Galileo;
			 if(!NN.Galileo||(Schnittstelle0.ist_eine_meiner_Var(vari)&&ist_MBM  
																))
			{
				nach_Galileo=				nach_Galileo ;
			}
			if(true)//if(!NN.Galileo)//if(!NN.Galileo||(Schnittstelle0.ist_eine_meiner_Var(vari)&&bm==1 //d.h. MBM
							//										))
				{ 
				double Speicher=0.5*Zellnetz.get_dt()*Zellnetz.get_dt()*vari.gibt_a(false);
				if(Schnittstelle0.ist_eine_meiner_Var(vari))
				;;;;
				vari.Kommazahl+=
						Speicher;   
				}
			} 
		}
}

class topologischeBoltzmannmaschine extends Boltzmannmaschine
{ 
	private int Reihe,Gruppe,Dimension;
	private double Spalt,Leerstelle;
	public static boolean Torus;   
	private static double Abklingkonstantekopie;
	public topologischeBoltzmannmaschine(int Beziehungen,int Reihe)
	{
		this(Beziehungen,Reihe,2/*Spalt*/,80/*Leerstelle*/,2/*Gruppe*/,2/*Dimension*/); 
	}
	public topologischeBoltzmannmaschine(int Beziehungen,int Reihe,double Spalt,double Leerstelle,int Gruppe,int Dimension)
	{
		super(Beziehungen,Reihe*Gruppe*Dimension);
		this.Reihe=Reihe;
		this.Gruppe=Gruppe;
		this.Dimension=Dimension;
		this.Spalt=Spalt;
		this.Leerstelle=Leerstelle;	
		this.Torus=true; 
		if(Beispiel==null)
			Beispiel=this;
		Abklingkonstantekopie=-1;
	}
	public double Breite(){
		if(vernicht_mich_455465465465) if(Sache()*Reihe()+Spalt()*(Reihe()-1)+Leerstelle<=0)
				throw new AssertionError();
		return Sache()*Reihe()+Spalt()*(Reihe()-1)+Leerstelle;
	}
	public double dieser_Spalt(){
		return Spalt;
	}
	public static double Spalt(){
		return Beispiel().Spalt;
	}
	public double Abstand_zwischen_Kernen(){
		return Spalt()+Sache();
	}
	public static double Abstand_zw(){
		return Beispiel().Abstand_zwischen_Kernen();
	}
	public double Reihe(){
		return Reihe;
	}
	public static int Sache(){
		return 7;
	}
	public int Dimension(){
		return Dimension;
	}
	public int Speicherabstand(){
		return Gruppe*Dimension();
	}
	public void setzt_neue_Stelle(boolean Galileo,Interface Schnittstelle0,boolean ist_MBM)
		{
		super.setzt_neue_Stelle(Galileo,Schnittstelle0,ist_MBM);
		grenzen();
		}	
	public void setzt_neue_Stelle(){
		
		super.setzt_neue_Stelle( ); 
		grenzen();			 
	}
		 
	public void grenzen()
		{ 
		int B=(int)(double)Breite();
		for(int i=0;i<number_of_variables;i++)
			{
			Stelle vari=variables[i];
			if(Torus)
			{
				while( vari.Kommazahl <0) 
					vari.Kommazahl+=B;
				vari.Kommazahl%=B;
			}
			else
			{
				if( vari.Kommazahl <0) 
					vari.Kommazahl=0;
				else
				if(vari.Kommazahl>B-1)
				vari.Kommazahl=B-1;
				if(vari.Kommazahl>B-11)//sonst kann es nicht sehen   546846874684864
				vari.Kommazahl=B-11;
			}
			}
		}
		
	public static double ausgerichtete_Entfernung(double E)
	{
		return Beispiel().ausgerichtete_Entfernung_0(E); 
	}
	private static topologischeBoltzmannmaschine Beispiel;
	public  static topologischeBoltzmannmaschine Beispiel()
	{
		if(Beispiel==null)
			throw new AssertionError("topologischeBoltzmannmaschine.Beispiel->java.lang.NullPointerException");
		return Beispiel;
	}
	private double ausgerichtete_Entfernung_0(double E)//bitte verändert auch abklingen(double x,double y) 
	{
		if(!Torus) 
			return E;
		double B;
		if( 2*E>(B=Breite()))
			return E-B;  
		if(-2*E>B)
			return E+B;  
		// nem (| b - a | > E/2) 
		return E;
	} 
	public static void setzt_sie_fest(double Abklingkonstante){
		if(!(Abklingkonstantekopie==-1))
			throw new AssertionError("Sie wurde von Interface2() schon angerufen!");
		Abklingkonstantekopie=Abklingkonstante; 
	}
	public static double abklingen(double x,double y)
	{	
		if(true)
			if(Beispiel().Abklingkonstantekopie==-1)
				throw new AssertionError("topologischeBoltzmannmaschine.Beispiel->Abklingkonstantekopie wurde nicht initialisiert ");
		return Beispiel().abklingen_versteckte(x,y); 
	}   
	private static double abklingen_versteckte(double x,double y)//ergibt schneller x+Abklingkonstantekopie*Beispiel().ausgerichtete_Entfernung_0(y-x)
	{
		double E=y-x;
		if(!Torus) 
			return x+E*Abklingkonstantekopie;
		double B;
		if( 2*E>(B=Beispiel.Breite())){
			double Ergebnis= x+(E-B)*Abklingkonstantekopie;  
			if(Ergebnis<0)
				Ergebnis+=B;
			return Ergebnis;
		}
		if(-2*E>B){
			double Ergebnis= x+(E+B)*Abklingkonstantekopie; 
			if(Ergebnis>B)
				Ergebnis-=B;
			return Ergebnis;
		} 
		// nem (| b - a | > E/2) 
		return x+E*Abklingkonstantekopie;
	}
		
	public static final boolean   die_Grenzen_verbessern = false; //vernicht_mich_871767116977
	public void schuetteln(double wie_viel)
		{ 
		if(!die_Grenzen_verbessern)
		{ 
			{ 
			for(int i=0;i<number_of_variables;i++) 
				{
				variables[i].setzt_a_fest(0); 
				if(!NN.Galileo)
					variables[i].setzt_a_fest(0,false); 
				variables[i].v=0; 
				}
			double kleiner=256/Breite();
			int z;
			int in_der_Reihe;
			for(z=0,in_der_Reihe=0;z<number_of_variables;z+=Gruppe*Dimension,in_der_Reihe++)
				{
				variables[z]  .Kommazahl=in_der_Reihe*Abstand_zwischen_Kernen();
				variables[z+1].Kommazahl=200/kleiner;
				variables[z+2].Kommazahl=in_der_Reihe*Abstand_zwischen_Kernen();
				variables[z+3].Kommazahl=(200-110)/kleiner;  
				if(false) //Es gäbe keinen Schwung am Amfang
					{
					if(in_der_Reihe==0) 
						variables[z+3].Kommazahl+=(Math.random()*2-1)*20/kleiner; 
					if((in_der_Reihe>2.*Reihe/5)&&(in_der_Reihe<3.*Reihe/5))
						variables[z+3].Kommazahl+=Math.random()*109/kleiner;   
					}
				else  {
					if((in_der_Reihe==0))
						variables[z+3].v=(Math.random()*2-1)*5/kleiner; 
					if(false)
						if( (in_der_Reihe==Reihe-1))
							variables[z+3].v=(Math.random()*2-1)*1/kleiner; 
					if((in_der_Reihe>2.*Reihe/5)&&(in_der_Reihe<3.*Reihe/5))
						variables[z+3].v+=Math.random()*20*wie_viel/kleiner;   
					}
				} 
			} 
			for(int z=0 ;z<number_of_variables;z+=Dimension ){//vernicht_mich_684684544215
				int x=(int)variables[z]    .Kommazahl;
				int y=(int)variables[z+1]  .Kommazahl; 
				if((x<0)||(!(x<Breite()))||(y<0)||(!(y<Breite())))
					        throw new AssertionError("u   "+x+" "+y+" "+"x<0 oder ...");
			}
		}
		else
		if(die_Grenzen_verbessern)
		{ 
			{ 
			for(int i=0;i<number_of_variables;i++) 
				{
				variables[i].setzt_a_fest(0); 
				if(!NN.Galileo)
					variables[i].setzt_a_fest(0,false); 
				variables[i].v=0; 
				}
			double kleiner=256/Breite();
			int z;
			int in_der_Reihe;
			for(z=0,in_der_Reihe=0;z<number_of_variables;z+=Gruppe*Dimension,in_der_Reihe++)
				{
				variables[z]  .Kommazahl=in_der_Reihe*Abstand_zwischen_Kernen();
				variables[z+1].Kommazahl=200/kleiner;
				variables[z+2].Kommazahl=in_der_Reihe*Abstand_zwischen_Kernen();
				variables[z+3].Kommazahl=(200-110)/kleiner;  
				  
				} 
			}
			if(true)//Beschleunigung am Amfang
				{
				variables[2].v=0; 
				variables[3].v=Math.random()*+50; 
				}
			for(int z=0 ;z<number_of_variables;z+=Dimension ){//vernicht_mich_684684544215
				int x=(int)variables[z]    .Kommazahl;
				int y=(int)variables[z+1]  .Kommazahl; 
				if((x<0)||(!(x<Breite()))||(y<0)||(!(y<Breite())))
					        throw new AssertionError("u   "+x+" "+y+" "+"x<0 oder ...");
			}
		} 
	}

}



class Interface2 extends BoltzmannmaschineOderInterface{
	Menge Mengen[];
	int Zahl_von_der_Mengen;
	topologischeBoltzmannmaschine[] BM;
	int Zahl_von_der_BM;
	double Abklingkonstante;
		
	private class Menge extends Stelle{
		Stelle[] Beruehrten;
		double[] wie_viel; 
		public Menge(){
			if(!(Zahl_von_der_Mengen<20))
				throw new AssertionError("Kein Problem.");
			Beruehrten  =new Stelle[Zahl_von_der_BM];
			wie_viel	=new double  [Zahl_von_der_BM];
			Mengen[Zahl_von_der_Mengen++]=this;	
		}
		public void beruehrt(topologischeBoltzmannmaschine Maschine,int wo,double wie_viel){
			beruehrt(Maschine,Maschine.variables[wo],wie_viel); 
		}
		public void beruehrt(topologischeBoltzmannmaschine Maschine,Stelle wo,double wie_viel){
			int BMnummer=0;
			while(BM[BMnummer]!=Maschine){
				BMnummer++;
				if(ENTWICKLUNG_MODUS)
					if(BMnummer==Zahl_von_der_BM)
						throw new AssertionError();
			}
			   Beruehrten   [BMnummer]=wo;
			this.wie_viel  [BMnummer]=wie_viel;
		}
		public boolean richtige_Gewichte(){
			double Gewicht=0;
			for(int i=0;i<Zahl_von_der_BM;i++)
				Gewicht+=wie_viel[i];
			if(Gewicht==1)
				return true;
			return false;
		}
		public Stelle der_Durchschnitt(){
			return (Stelle)this;
		}
	}
	public boolean richtige_Gewichte(){
		boolean aua=false;
		for(int b=0;b<Zahl_von_der_Mengen;b++){
			if(!Mengen[b].richtige_Gewichte())
				aua=true;
		}
		if(!aua)
			return true;
		if(!ENTWICKLUNG_MODUS)
			throw new AssertionError("Es gibt einen Fehler von dem Programmierer.");
		System.out.println("Keine richtige_Gewichte:"); 
		for(int b=0;b<Zahl_von_der_Mengen;b++) {
				double Gesamtgewicht=0;
				for(int i=0;i<Zahl_von_der_BM;i++)
					Gesamtgewicht+=Mengen[b].wie_viel[i];
				if(Gesamtgewicht!=1){
				System.out.print ("Menge "+b); 
					System.out.println( ": Gesamtgewicht="+Gesamtgewicht ); 
				}
			}
			;// schreib´ da!
		return false;
	}
	
	private Interface2(topologischeBoltzmannmaschine[] BM,int Zahl_von_der_BM){//private for Nagel. Kein Problem.
		super(0);
		this.BM=new topologischeBoltzmannmaschine[this.Zahl_von_der_BM=Zahl_von_der_BM];
		for(int i=0;i<Zahl_von_der_BM;i++)
			this.BM[i]=BM[i];
		Mengen=new Menge[20]; 
		Zahl_von_der_Mengen=0;
		Abklingkonstante=0.9;
		topologischeBoltzmannmaschine.setzt_sie_fest(Abklingkonstante);
	} 
	private Interface2(){//private for Nagel. Kein Problem.
		this(new topologischeBoltzmannmaschine[] {},0); 
	}
	public Interface2(topologischeBoltzmannmaschine[] BM,int Zahl_von_der_BM,int Nagel){
		this(BM,Zahl_von_der_BM);
		naehen(Nagel);
	}
	public void add(topologischeBoltzmannmaschine bm){//bitte "private"
		if(Zahl_von_der_Mengen!=0)
			throw new AssertionError("\"add\" jetzt!?");//System.out.println("\"add\" jetzt!?");
		BM[Zahl_von_der_BM++]=bm;
		}
	public static final int Links=0;
	public static final int Halbe_Halbe=101;
	public static final int Fuers_Rad=100;
	public void naehen(int Gestalt){
		switch(Gestalt){
			case Links:{
				if(Zahl_von_der_BM<2)
					throw new AssertionError();
				Menge Mx=new Menge();
				Menge My=new Menge();
				Mx.beruehrt(BM[0],2,1);
				Mx.beruehrt(BM[1],2,0);
				My.beruehrt(BM[0],3,1);
				My.beruehrt(BM[1],3,0); 
			break;
			}
			case Halbe_Halbe:{//halbe-halbe  //FUNKTIONNIERT NICHT
				if(Zahl_von_der_BM<2)
					throw new AssertionError();
				Menge Mx=new Menge();
				Menge My=new Menge();
				Mx.beruehrt(BM[0],2,0.5);
				Mx.beruehrt(BM[1],2,0.5);
				My.beruehrt(BM[0],3,0.5);
				My.beruehrt(BM[1],3,0.5);  
			break;
			}
			case Fuers_Rad:{
				if(Zahl_von_der_BM<2)
					throw new AssertionError();
				if(BM[0].Reihe()<20)
					throw new AssertionError();
				Menge Mx=new Menge();
				Menge My=new Menge();
				int Sa=BM[0].Speicherabstand();
				int last=19*Sa+BM[0].Dimension();
				Mx.beruehrt(BM[0],last+0,1);
				Mx.beruehrt(BM[1],last+0,0);
				My.beruehrt(BM[0],last+1,1);
				My.beruehrt(BM[1],last+1,0); 
				Menge M3=new Menge();
				M3.beruehrt(BM[0],last-Sa,1);
				M3.beruehrt(BM[1],last-Sa,0); 
				Menge M4=new Menge();
				M4.beruehrt(BM[0],last-Sa+1,1);
				M4.beruehrt(BM[1],last-Sa+1,0);  
			break;
			}
			default:
				throw new AssertionError("Eine solche Gestalt gibt´s nicht.");
		}
		if(!richtige_Gewichte())
			throw new AssertionError();
	}
	public void erzwingt(){
		if(ENTWICKLUNG_MODUS){
			if(Zahl_von_der_BM<1) 
				throw new AssertionError("Es gibt keine Maschinen!");
			if(Zahl_von_der_Mengen==0)
						throw new AssertionError("Ich zweifle.");
			if(!richtige_Gewichte()) 
				throw new AssertionError("Manchmal sind die Gewichte falsch.");
		}
		
		{//druecken
			for(int bm=0;bm<Zahl_von_der_BM;bm++)
				for(int i=0;i<BM[bm].number_of_variables;i++) 
					BM[bm].variables[i].a=0;   
			for(int bm=0;bm<Zahl_von_der_BM;bm++)
				BM[bm].druecken(true); 
		}
		if(NN.uebereinstimmen&&!nurEBM ){
			for(int Mn=0;Mn<Zahl_von_der_Mengen;Mn++){//das Rechnen der Durchschnitte 
				Menge dieMenge=Mengen[Mn];
				Stelle der_Durchschnitt=dieMenge.der_Durchschnitt(); 
				der_Durchschnitt.Kommazahl=0; 
				der_Durchschnitt.v=0; 
				der_Durchschnitt.a=0;  
				for(int bm=0;bm<Zahl_von_der_BM;bm++){
					double wie_viel=dieMenge.wie_viel[bm]; 
					der_Durchschnitt.Kommazahl+=dieMenge.Beruehrten[bm].Kommazahl*wie_viel; 
					der_Durchschnitt.v+=		dieMenge.Beruehrten[bm].v*wie_viel; 
					der_Durchschnitt.a+=		dieMenge.Beruehrten[bm].a*wie_viel; 
				}			
			}//bitte den aperiodischen Grenzfall von genagelt.. hierher implementieren 
			for(int Mn=0;Mn<Zahl_von_der_Mengen;Mn++){//ihres Erzwingen 
				Menge dieMenge=Mengen[Mn];
				Stelle der_Durchschnitt=dieMenge.der_Durchschnitt(); 
				for(int bm=0;bm<Zahl_von_der_BM;bm++){
						//double wie_viel=dieMenge.wie_viel[bm]; 
						Stelle B=dieMenge.Beruehrten[bm];  
						B.Kommazahl= 
						      topologischeBoltzmannmaschine.abklingen
							(B.Kommazahl,der_Durchschnitt.Kommazahl);  
							
						B.v+=		 Abklingkonstante*	
							(der_Durchschnitt.v-B.v);
						B.a+=		 Abklingkonstante*	
							(der_Durchschnitt.a-B.a); 
					}
			} 
		}
		for(int bm=0;bm<Zahl_von_der_BM;bm++)//drueckt
				{ 
				BM[bm].setzt_neue_Stelle();	
				} 	 			 			
	}
	public static void druecken(Interface2 Schnittstelle)
		{  
		druecken(Schnittstelle,Schnittstelle.BM,Schnittstelle.Zahl_von_der_BM);
		} 
	public static void druecken(Interface2 Schnittstelle,Boltzmannmaschine[] alleMaschinen,int Zahl_von_der_BM)
		{
		druecken_(Schnittstelle,alleMaschinen,Zahl_von_der_BM);
		} 
	private static void druecken_(Interface2 Schnittstelle,Boltzmannmaschine[] alleMaschinen,int Zahl_von_der_BM)
		{   
		
			{
			if(Zahl_von_der_BM<2)
				throw new AssertionError("Scheiße!: Zahl_von_der_BM ist kleiner als 2...");
			if((alleMaschinen==null) )
				throw new AssertionError("Scheiße!");  
			} 
		Schnittstelle.erzwingt();   					 
		}
	public void setzt_sie_fest(double Abklingkonstante){
		if(!(this.Abklingkonstante==1))
			throw new AssertionError("Es gibt einen Fehler von dem Programmierer.");
		this.Abklingkonstante=Abklingkonstante; 
	}
	public topologischeBoltzmannmaschine eine_Maschine(){
		if(Zahl_von_der_BM<1)
			throw new AssertionError("Es gibt einen Fehler von dem Programmierer.");
		return BM[0];		
	}
} 