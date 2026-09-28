/* jlooch -- make them droney sounds once again, using JSyn
 *
 * 		Brad Garton, fall 2001
 *
*/
import java.util.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.applet.Applet;
import com.softsynth.jsyn.*;

public class jlooch extends Applet implements AdjustmentListener, ActionListener
{
	Scrollbar DroneScroll;
	Scrollbar SeqScroll;
	Scrollbar WarbleScroll;
	Scrollbar BurstScroll;
	Scrollbar VolumeScroll;
	Label volumeReadout;
	Button goButton;
	Button [] onoffs = new Button[4];
	int [] onoffstates = { 1, 1, 1, 1 };
	myCanvas drawArea;
	URL netimage;

	// synthesis stuff
	DroneNotes droneyThread;
	SeqNotes seqThread;
	WarbleNote warbleThread;
	BurstNote burstThread;

	// window and control layout -- every slider column shares the same geometry
	// four voice columns plus a master volume column on the right
	static final int COLW = 110;
	static final int W = 5 * COLW, H = 600;
	static final int SLIDEW = 36, SLIDEH = 360, SLIDEY = 100;
	static final int ONOFFW = 30, ONOFFH = 26, ONOFFY = 64;
	static final int LABELY = SLIDEY + SLIDEH + 24;
	static final int GOW = 130, GOH = 32, GOY = LABELY + 22;
	static final String [] labels = { "Drones", "Sequences", "Warbles", "Noises", "Volume" };

	public static void main(String args[])
	{
		jlooch wooflet = new jlooch();
		AppletFrame f = new AppletFrame("bark!  bark!", wooflet);
		f.setIconImage(Toolkit.getDefaultToolkit().getImage("loochicon.gif"));
		f.show();
		Insets in = f.getInsets();
		f.setSize(W + in.left + in.right, H + in.top + in.bottom);
		f.test();
		f.setResizable(false);
	}

	Scrollbar makeSlider(int col, String name, int value, Color fc)
	{
		Scrollbar s = new Scrollbar(Scrollbar.VERTICAL, value, 10, 0, 110);
		s.setBounds(col*COLW + (COLW-SLIDEW)/2, SLIDEY, SLIDEW, SLIDEH);
		s.setName(name);
		s.setBackground(fc);
		s.addAdjustmentListener(this);
		add(s);
		return s;
	}

	public void init()
	{
		Font bfont;
		int i;

		// place everything by hand; the default FlowLayout ignores our bounds
		setLayout(null);

		DroneScroll  = makeSlider(0, "drones",  0,  new Color((float)0.1, (float)0.7, (float)0.7));
		SeqScroll    = makeSlider(1, "seqs",    79, new Color((float)0.2, (float)0.6, (float)0.7));
		WarbleScroll = makeSlider(2, "warbles", 86, new Color((float)0.3, (float)0.5, (float)0.7));
		BurstScroll  = makeSlider(3, "bursts",  88, new Color((float)0.4, (float)0.4, (float)0.7));
		VolumeScroll = makeSlider(4, "volume",  0,  new Color((float)0.3, (float)0.3, (float)0.4));

		volumeReadout = new Label("100%", Label.CENTER);
		volumeReadout.setBounds(4*COLW + (COLW-60)/2, ONOFFY, 60, ONOFFH);
		volumeReadout.setFont(new Font("Helvetica", Font.BOLD, 14));
		volumeReadout.setBackground(Color.white);
		add(volumeReadout);

		Color fc = new Color((float)0.9, (float)0.1, (float)0.2);
		bfont = new Font("Helvetica", Font.BOLD, 14);
		for (i = 0; i < 4; i++)
		{
			onoffs[i] = new Button();
			onoffs[i].setBounds(i*COLW + (COLW-ONOFFW)/2, ONOFFY, ONOFFW, ONOFFH);
			onoffs[i].setFont(bfont);
			onoffs[i].setBackground(fc);
			onoffs[i].setForeground(Color.yellow);
			onoffs[i].setLabel("+");
			onoffs[i].addActionListener(this);
			add(onoffs[i]);
		}
		onoffs[0].setName("drones");
		onoffs[1].setName("seqs");
		onoffs[2].setName("warbles");
		onoffs[3].setName("bursts");

		fc = new Color((float)0.2, (float)0.7, (float)0.8);
		goButton = new Button();
		goButton.setBounds((W-GOW)/2, GOY, GOW, GOH);
		goButton.setBackground(fc);
		bfont = new Font("Times", Font.ITALIC, 16);
		goButton.setFont(bfont);
		fc = new Color((float)0.9, (float)0.1, (float)0.1);
		goButton.setForeground(fc);
		goButton.setLabel("drono...");
		goButton.setName("main");
		goButton.addActionListener(this);
		add(goButton);

		try {
			netimage = new URL("file:./loochicon.gif");
//			netimage = new URL("http://music.columbia.edu/~brad/jlooch/loochicon.gif");
		} catch(MalformedURLException e) {
			System.err.println("no image");
			return;
		}
		drawArea = new myCanvas(netimage);
		drawArea.setBackground(Color.white);
		drawArea.setBounds(0, 0, W, H);
		add(drawArea);
	}

	public void start()
	{
		drawArea.start();

		try
		{
			Synth.startEngine(0);

			droneyThread = new DroneNotes();
			seqThread = new SeqNotes();
			warbleThread = new WarbleNote();
			burstThread = new BurstNote();
		} catch(SynthException e) {
			SynthAlert.showError(this,e);
		}
	}

	public void stop()
	{
		try
		{
			if (going == 1) {
				if (onoffstates[0] == 1) {
					droneyThread.stopSound(); }
				if (onoffstates[1] == 1) {
					seqThread.stopSound(); }
				if (onoffstates[2] == 1) {
					warbleThread.stopSound(); }
				if (onoffstates[3] == 1) {
					burstThread.stopSound(); }
			}
			droneyThread.stop();
			seqThread.stop();
			warbleThread.stop();
			burstThread.stop();
			if (started == 1) {
				Synth.stopEngine();
			}
		} catch(SynthException e) {
			SynthAlert.showError(this,e);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		Scrollbar theScroll = (Scrollbar)e.getAdjustable();
		int value;
		double prob;

		value =  theScroll.getValue();
		prob = (double)(100-value)/100.0;

		// master volume works whether or not the voices are playing
		if (theScroll == VolumeScroll) {
			volumeReadout.setText((100-value) + "%");
			try {
				VolumeOut.setVolume(prob);
			} catch(SynthException se) {
				SynthAlert.showError(this,se);
			}
			return;
		}

		if (started == 1) {
			if (theScroll.getName() == "drones") {
				droneyThread.setProb(prob);
			}
			if (theScroll.getName() == "seqs") {
				seqThread.setProb(prob);
			}
			if (theScroll.getName() == "warbles") {
				warbleThread.setProb(prob);
			}
			if (theScroll.getName() == "bursts") {
				burstThread.setProb(prob);
			}
		}
	}

	int going = 0;
	int started = 0;
	public void actionPerformed(ActionEvent e)
	{
		int switcher = 4;
		Color c,goc,stopc;
		Button theButton = (Button)e.getSource();


		goc = new Color((float)0.9, (float)0.1, (float)0.2);
		stopc = new Color((float)0.1, (float)0.8, (float)0.7);
		if (theButton.getName() == "drones") { switcher = 0; }
		if (theButton.getName() == "seqs") { switcher = 1; }
		if (theButton.getName() == "warbles") { switcher = 2; }
		if (theButton.getName() == "bursts") { switcher = 3; }
		if (theButton.getName() == "main") { switcher = 4; }
	    try {
		switch (switcher) {
			case 0:
				if (going == 1) {
					if (onoffstates[0] == 0) {
						droneyThread.start();
						droneyThread.setProb((100.0-(double)DroneScroll.getValue())/100.0);
						onoffs[0].setBackground(goc);
						onoffs[0].setForeground(Color.yellow);
						onoffs[0].setLabel("+");
						onoffstates[0] = 1;
					} else {
						droneyThread.stopSound();
						onoffs[0].setBackground(stopc);
						onoffs[0].setForeground(Color.white);
						onoffs[0].setLabel("-");
						onoffstates[0] = 0;
					}
				} else {
					if (onoffstates[0] == 0) {
						onoffs[0].setBackground(goc);
						onoffs[0].setForeground(Color.yellow);
						onoffs[0].setLabel("+");
						onoffstates[0] = 1;
					} else {
						onoffs[0].setBackground(stopc);
						onoffs[0].setForeground(Color.white);
						onoffs[0].setLabel("-");
						onoffstates[0] = 0;
					}
				}
				break;
			case 1:
				if (going == 1) {
					if (onoffstates[1] == 0) {
						seqThread.start();
						seqThread.setProb((100.0-(double)SeqScroll.getValue())/100.0);
						onoffs[1].setBackground(goc);
						onoffs[1].setForeground(Color.yellow);
						onoffs[1].setLabel("+");
						onoffstates[1] = 1;
					} else {
						seqThread.stopSound();
						onoffs[1].setBackground(stopc);
						onoffs[1].setForeground(Color.white);
						onoffs[1].setLabel("-");
						onoffstates[1] = 0;
					}
				} else {
					if (onoffstates[1] == 0) {
						onoffs[1].setBackground(goc);
						onoffs[1].setForeground(Color.yellow);
						onoffs[1].setLabel("+");
						onoffstates[1] = 1;
					} else {
						onoffs[1].setBackground(stopc);
						onoffs[1].setForeground(Color.white);
						onoffs[1].setLabel("-");
						onoffstates[1] = 0;
					}
				}
				break;
			case 2:
				if (going == 1) {
					if (onoffstates[2] == 0) {
						warbleThread.start();
						warbleThread.setProb((100.0-(double)WarbleScroll.getValue())/100.0);
						onoffs[2].setBackground(goc);
						onoffs[2].setForeground(Color.yellow);
						onoffs[2].setLabel("+");
						onoffstates[2] = 1;
					} else {
						warbleThread.stopSound();
						onoffs[2].setBackground(stopc);
						onoffs[2].setForeground(Color.white);
						onoffs[2].setLabel("-");
						onoffstates[2] = 0;
					}
				} else {
					if (onoffstates[2] == 0) {
						onoffs[2].setBackground(goc);
						onoffs[2].setForeground(Color.yellow);
						onoffs[2].setLabel("+");
						onoffstates[2] = 1;
					} else {
						onoffs[2].setBackground(stopc);
						onoffs[2].setForeground(Color.white);
						onoffs[2].setLabel("-");
						onoffstates[2] = 0;
					}
				}
				break;
			case 3:
				if (going == 1) {
					if (onoffstates[3] == 0) {
						burstThread.start();
						burstThread.setProb((100.0-(double)BurstScroll.getValue())/100.0);
						onoffs[3].setBackground(goc);
						onoffs[3].setForeground(Color.yellow);
						onoffs[3].setLabel("+");
						onoffstates[3] = 1;
					} else {
						burstThread.stopSound();
						onoffs[3].setBackground(stopc);
						onoffs[3].setForeground(Color.white);
						onoffs[3].setLabel("-");
						onoffstates[3] = 0;
					}
				} else {
					if (onoffstates[3] == 0) {
						onoffs[3].setBackground(goc);
						onoffs[3].setForeground(Color.yellow);
						onoffs[3].setLabel("+");
						onoffstates[3] = 1;
					} else {
						onoffs[3].setBackground(stopc);
						onoffs[3].setForeground(Color.white);
						onoffs[3].setLabel("-");
						onoffstates[3] = 0;
					}
				}
				break;
			case 4:
				started = 1;
				if (going == 1)
				{
					c = new Color((float)0.2, (float)0.7, (float)0.8);
					goButton.setBackground(c);
					c = new Color((float)0.9, (float)0.1, (float)0.1);
					goButton.setForeground(c);
					goButton.setLabel("drono...");

					if (onoffstates[0] == 1) {
						droneyThread.stopSound();
					}
					if (onoffstates[1] == 1) {
						seqThread.stopSound();
					}
					if (onoffstates[2] == 1) {
						warbleThread.stopSound();
					}
					if (onoffstates[3] == 1) {
						burstThread.stopSound();
					}
					going = 0;
				} else {
					c = new Color((float)0.3, (float)0.7, (float)0.7);
					goButton.setBackground(c);
					c = new Color((float)0.9, (float)0.1, (float)0.1);
					goButton.setForeground(c);
					goButton.setLabel("StopEm!");

					if (onoffstates[0] == 1) {
						droneyThread.start();
						droneyThread.setProb((100.0-(double)DroneScroll.getValue())/100.0);
					}
					if (onoffstates[1] == 1) {
						seqThread.start();
						seqThread.setProb((100.0-(double)SeqScroll.getValue())/100.0);
					}
					if (onoffstates[2] == 1) {
						warbleThread.start();
						warbleThread.setProb((100.0-(double)WarbleScroll.getValue())/100.0);
					}
					if (onoffstates[3] == 1) {
						burstThread.start();
						burstThread.setProb((100.0-(double)BurstScroll.getValue())/100.0);
					}
					going = 1;
				}
				break;	
		}
	    } catch(SynthException se) {
		SynthAlert.showError(this,se);
	    }
	}


}

class myCanvas extends Canvas
{
	Graphics cg,bg;
	Speckle goDots;
	Image bstore;
	Image loochimage;
	MediaTracker tracker = new MediaTracker(this);

	public myCanvas(URL db)
	{
		super();
		loochimage = Toolkit.getDefaultToolkit().getImage(db);
		goDots = new Speckle(jlooch.W, jlooch.H);
	}

	public void start()
	{
		bstore = createImage(jlooch.W, jlooch.H);
		tracker.addImage(bstore, 0);
		tracker.addImage(loochimage, 1);
		try {
			tracker.waitForID(0);
			tracker.waitForID(1);
		} catch (InterruptedException e) {
			System.err.println(e);
		}

		cg = this.getGraphics();
		bg = bstore.getGraphics();
		goDots.setGraphics(cg, bg);
		goDots.start();
	}

	public void paint(Graphics g)
	{
		Font lfont;

		// no idea why I have to do this... the MediaTracker
		// should catch these errors, I thought
		while ( (bstore == null) || (loochimage == null) )
		{
			try {
				java.lang.Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
		g.drawImage(bstore, 0, 0, Color.white, this);
		// the icon is tiny (31x21), so show it at double size
		int iw = loochimage.getWidth(this) * 2, ih = loochimage.getHeight(this) * 2;
		g.drawImage(loochimage, 6, jlooch.H - ih - 8, iw, ih, Color.white, this);

		g.setColor(Color.black);
		lfont = new Font("Times", Font.BOLD, 22);
		g.setFont(lfont);
		drawCentered(g, "Spirit of the Looch", jlooch.W/2, 38);

		// name each slider, centered under its column
		lfont = new Font("Helvetica", Font.BOLD, 14);
		g.setFont(lfont);
		for (int i = 0; i < jlooch.labels.length; i++) {
			drawCentered(g, jlooch.labels[i], i*jlooch.COLW + jlooch.COLW/2, jlooch.LABELY);
		}

		// set the volume column apart from the four voices
		g.setColor(Color.lightGray);
		g.drawLine(4*jlooch.COLW, jlooch.ONOFFY, 4*jlooch.COLW, jlooch.LABELY + 6);
		g.setColor(Color.black);

		lfont = new Font("Helvetica", Font.PLAIN, 11);
		g.setFont(lfont);
		drawCentered(g, "more", 18, jlooch.SLIDEY + 12);
		drawCentered(g, "less", 18, jlooch.SLIDEY + jlooch.SLIDEH - 4);
		g.drawString("Brad Garton", jlooch.W - 80, jlooch.H - 12);
	}

	void drawCentered(Graphics g, String s, int cx, int y)
	{
		g.drawString(s, cx - g.getFontMetrics().stringWidth(s)/2, y);
	}
}

