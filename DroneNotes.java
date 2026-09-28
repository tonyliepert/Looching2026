/* DroneNotes -- notes that drone
 * part of the jlooch app
 *
 *		Brad Garton	10/2001
 */

import java.util.*;
import java.awt.*;
import com.softsynth.jsyn.*;

class DroneThread extends Thread
{
	public double		prob = 1.0;
	boolean			go = true;
	TableOscillator [] 	droneOsc = new TableOscillator[2];
	AddUnit			oscMixer;
	SynthTable 		oscTable;
	EnvelopePlayer 		droneEnvPlayer;
	SynthEnvelope 		droneEnv;
	VolumeOut 		droneOut;
	double [] 		tData = new double[257];
	Filter_LowPass 		droneFilt;
	DelayUnit		del1;
	DelayUnit		del2;
	AddUnit			del1inMixer;
	AddUnit			outAMixer;
	MultiplyUnit		feedBack;
	EnvelopePlayer 		filtEnvPlayer;
	SynthEnvelope		filtEnv;
	double [] 		fData = new double[4];
	double [] notes =
	{
		5.05, 5.07, 5.10, 6.00, 6.05, 6.10,
		7.00, 7.03, 7.05, 7.07, 7.10, 8.00,
		8.02, 8.03, 8.05, 8.07, 8.10, 9.00
	};

	public DroneThread() throws SynthException
	{
		int i;

		droneOsc[0] = new TableOscillator();
		droneOsc[1] = new TableOscillator();
		oscMixer = new AddUnit();
		droneFilt = new Filter_LowPass();
		del1= new DelayUnit(0.35);
		del2= new DelayUnit(0.49);
		droneEnvPlayer = new EnvelopePlayer();
		filtEnvPlayer = new EnvelopePlayer();
		droneOut = new VolumeOut();
		del1inMixer = new AddUnit();
		outAMixer = new AddUnit();
		feedBack = new MultiplyUnit();
		

		droneOsc[0].output.connect(oscMixer.inputA);
		droneOsc[1].output.connect(oscMixer.inputB);
		oscMixer.output.connect(droneFilt.input);
		filtEnvPlayer.output.connect(droneFilt.frequency);
		droneFilt.output.connect(droneEnvPlayer.amplitude);
		droneEnvPlayer.output.connect(outAMixer.inputA);
		del2.output.connect(outAMixer.inputB);
		del2.output.connect(feedBack.inputA);
		outAMixer.output.connect(0, droneOut.left, 0);
		droneEnvPlayer.output.connect(del1inMixer.inputA);
		feedBack.output.connect(del1inMixer.inputB);
		del1inMixer.output.connect(del1.input);
		del1.output.connect(0, droneOut.right, 0);
		del1.output.connect(del2.input);

		double[] data =
		{
			5.0, 1.0,
			5.0, 0.0  
		};
		droneEnv = new SynthEnvelope(data);

		fData[0] = 5.0;
		fData[1] = 500.0;
		fData[2] = 5.0;
		fData[3] = 100.0;
		filtEnv = new SynthEnvelope(fData);

		for (i = 0; i < 256; i++)
		{
			tData[i] = Math.random() * 2.0 - 1.0;
		}
		tData[256] = tData[0];
		oscTable = new SynthTable(tData);

		droneOsc[0].tablePort.setTable(oscTable);
		droneOsc[1].tablePort.setTable(oscTable);

	}

	void stopSound()
	{
		try
		{
			droneOut.stop();
			droneOsc[0].stop();
			droneOsc[1].stop();
			oscMixer.stop();
			del1inMixer.stop();
			outAMixer.stop();
			feedBack.stop();
			droneFilt.stop();
			del1.stop();
			del2.stop();
			droneEnvPlayer.stop();
			filtEnvPlayer.stop();
		} catch (SynthException e) {
			System.err.println(e);
		}
	}
		
	public void halt()
	{
		go = false;
		interrupt();
	}

	public void run()
	{
		double freq;
		double wait;
		int basewait = 0;
		int i;

		try
		{
			droneEnvPlayer.start();
			filtEnvPlayer.start();
			droneOsc[0].start();
			droneOsc[1].start();
			oscMixer.start();
			del1inMixer.start();
			outAMixer.start();
			feedBack.start();
			droneFilt.start();
			del1.start();
			del2.start();
			droneOut.start();

			while (go == true)
			{
				droneOsc[0].amplitude.set(0.1);
				droneOsc[1].amplitude.set(0.1);
				freq = BradUtils.cpspch(BradUtils.chooseItem(notes));
				droneOsc[0].frequency.set(BradUtils.windowfreq(freq, 0.017));
				droneOsc[1].frequency.set(BradUtils.windowfreq(freq, 0.017));

				droneFilt.frequency.set(100.0);
				droneFilt.amplitude.set(1.0);
				droneFilt.Q.set(BradUtils.crandom(1.0, 15.0));
				feedBack.inputB.set(0.92);
				fData[0] = 5.0;
				fData[1] = BradUtils.crandom(100.0, 900.0);
				fData[2] = 5.0;
				fData[3] = BradUtils.crandom(100.0, 500.0);
				filtEnv.write(fData);
				filtEnvPlayer.envelopePort.clear();
				filtEnvPlayer.envelopePort.queue(filtEnv);
   				droneEnvPlayer.envelopePort.clear();
   				droneEnvPlayer.envelopePort.queue(droneEnv);

				wait = Math.random()*10.0 + 7.0;
				wait = wait * 689.0;
				basewait = basewait + (int)wait;
				Synth.sleepUntilTick(basewait);

				while (Math.random() > prob)
				{
					basewait = basewait + (15 * 689);
					Synth.sleepUntilTick(basewait);
				}

				for (i = 0; i < 256; i++)
				{
					tData[i] = Math.random() * 2.0 - 1.0;
				}
				tData[256] = tData[0];
				oscTable.write(tData);
			}
		} catch (SynthException e) {
			System.err.println(e);
		}
		stopSound();
	}
}


public class DroneNotes extends Thread
{
	DroneThread drones1;
	DroneThread drones2;
	DroneThread drones3;

	public void start()  
	{
		try
		{
			drones1 = new DroneThread();
			drones2 = new DroneThread();
			drones3 = new DroneThread();

			drones1.start();
			drones2.start();
			drones3.start();
		} catch(SynthException e) {
			SynthAlert.showError(e);
		}
	}

	public void stopSound()  
	{
		try
		{
			drones1.stopSound();
			drones2.stopSound();
			drones3.stopSound();
		} catch(SynthException e) {
			SynthAlert.showError(e);
		}
   	}

	public void setProb(double p)
	{
		drones1.prob = p;
		drones2.prob = p;
		drones3.prob = p;
	}
}
