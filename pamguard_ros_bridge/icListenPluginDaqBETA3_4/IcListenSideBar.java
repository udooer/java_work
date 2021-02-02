package icListenPluginDaqBETA3_4;

import java.awt.BorderLayout;
import java.awt.Dimension;
//import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.PamSidePanel;
import PamView.PamSlider;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
//import soundPlayback.FilePlaybackSideBar;
import soundPlayback.PlaybackControl;

/*
 * Creates sidebar slider for sound output gain for icListen
 * No longer used as pamguard disables it in newSettings() in playbackControl
 * Aishat Mohammed January 28th 2020
 */

class IcListenSideBar implements PamSidePanel {
	
	private PlaybackControl playbackControl;

	private SidePanel sidePanel;

	private PamSlider gainSlider;
	protected JLabel currGainValue;

	IcListenSideBar(PlaybackControl playbackControl){
		this.playbackControl = playbackControl;

		double minVal = -4;
		double maxVal = 4;
		double stepVal = 1;

		sidePanel = new SidePanel(playbackControl.getUnitName(), minVal, maxVal, stepVal, "X", 40);

	}


	private class SidePanel extends PamBorderPanel{

		private static final long serialVersionUID = 1L;

		//protected String unitString;
		protected String title;
		protected double minVal, maxVal, stepVal;
		//protected double stepScale;

		SidePanel(String title, double minVal, double maxVal, double stepVal, String units, int width) {
			//unitString = units;
			setBorder(new TitledBorder(title));
			this.title = title;
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.stepVal = stepVal;
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			add(gainSlider = new PamSlider());
			gainSlider.setMinimum(0);
			gainSlider.setMaximum(1000);
			gainSlider.setValue(playbackControl.getPlaybackParameters().playbackGain);
			gainSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					gainChanged();
				}
			});
			PamPanel pp = new PamPanel(new BorderLayout());
			pp.add(BorderLayout.CENTER, currGainValue = new PamLabel(" ", JLabel.CENTER));
			add(pp);
			

			if (width > 0) {
				Dimension d = gainSlider.getPreferredSize();
				d.width = width;
				gainSlider.setPreferredSize(d);
			}
			gainSlider.setToolTipText("Control playback gain");
			gainChanged();

		}

		int getSliderMaximum() {
			return (int) ((maxVal-minVal)/stepVal);
		}

		int getSliderMinimum() {
			return 0;
		}

	}


	@Override
	public JComponent getPanel() {
		return sidePanel;
	}

	protected void gainChanged() {
		playbackControl.getPlaybackParameters().playbackGain = gainSlider.getValue();	
		currGainValue.setText(String.format("Gain    %ddB", playbackControl.getPlaybackParameters().playbackGain));
	}

	@Override
	public void rename(String newName) {
		sidePanel.repaint();
	}

}
