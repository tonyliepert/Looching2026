/* VolumeOut -- stereo LineOut with a master volume shared by every voice
 * part of the jlooch app
 *
 * connect to left/right instead of LineOut.input parts 0/1
 */

import com.softsynth.jsyn.*;

class VolumeOut
{
	// one smoothed gain signal feeds every VolumeOut, so the slider doesn't click
	static LinearLag	masterGain;
	static double		gain = 1.0;

	public SynthInput	left, right;
	MultiplyUnit		leftAmp, rightAmp;
	LineOut			lineOut;

	public VolumeOut() throws SynthException
	{
		leftAmp = new MultiplyUnit();
		rightAmp = new MultiplyUnit();
		lineOut = new LineOut();

		left = leftAmp.inputA;
		right = rightAmp.inputA;
		leftAmp.output.connect(0, lineOut.input, 0);
		rightAmp.output.connect(0, lineOut.input, 1);

		LinearLag g = getMasterGain();
		g.output.connect(leftAmp.inputB);
		g.output.connect(rightAmp.inputB);
	}

	static synchronized LinearLag getMasterGain() throws SynthException
	{
		if (masterGain == null) {
			masterGain = new LinearLag();
			masterGain.time.set(0.05);
			masterGain.input.set(gain);
			masterGain.start();
		}
		return masterGain;
	}

	// level runs 0.0 to 1.0; squared so the slider feels even to the ear
	public static synchronized void setVolume(double level) throws SynthException
	{
		gain = level * level;
		if (masterGain != null) {
			masterGain.input.set(gain);
		}
	}

	public void start() throws SynthException
	{
		leftAmp.start();
		rightAmp.start();
		lineOut.start();
	}

	public void stop() throws SynthException
	{
		lineOut.stop();
		leftAmp.stop();
		rightAmp.stop();
	}
}
