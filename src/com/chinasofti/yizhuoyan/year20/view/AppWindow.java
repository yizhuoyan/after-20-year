package com.chinasofti.yizhuoyan.year20.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.jws.Oneway;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.chinasofti.yizhuoyan.year20.model.AppModel;
import com.chinasofti.yizhuoyan.year20.view.custom.ImageView;

public class AppWindow extends JFrame implements ActionListener, com.chinasofti.yizhuoyan.year20.model.AppModel.Callback {
	private static final long serialVersionUID = -4485286794323588463L;
	private final AppModel model;
	private JPanel nowPhotoPanel, futurePhotoPanel;
	private JFileChooser imgChooser;
	private ImageView nowPhotoIV, futurePhotoIV;
	private JButton actionBtn;
	private JComboBox<String> yearCbb;
	private JLabel messageLab;
	private JProgressBar progressBar;
	private Timer progressBarTimer; 
	public AppWindow(AppModel model) throws IOException{
		this.model = model;
		this.setTitle("20Years");
		this.setSize(360, 600);
		this.setIconImage(ImageIO.read(AppWindow.class.getResource("/icon.png")));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.initLayout();
	}

	private void initLayout() {
		Box box = new Box(BoxLayout.Y_AXIS);
		{// title
			JPanel titlePan = new JPanel(new BorderLayout());
			titlePan.setBackground(Color.BLACK);
			titlePan.setMaximumSize(new Dimension(this.getWidth(), 50));
			titlePan.setPreferredSize(titlePan.getMaximumSize());
			JLabel titleLab = new JLabel("20年后你的样子");
			titleLab.setForeground(Color.WHITE);
			titleLab.setHorizontalAlignment(JLabel.CENTER);
			titlePan.add(titleLab);
			box.add(titlePan);
		}
		{// gendal
			JPanel gendalPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel gendalLab = new JLabel("你的性别:");
			gendalPan.add(gendalLab);
			JRadioButton maleBtn = new JRadioButton("男");
			maleBtn.setSelected(true);
			model.setMale(true);
			maleBtn.setActionCommand("male");
			maleBtn.addActionListener(this);
			JRadioButton femaleBtn = new JRadioButton("女");
			femaleBtn.setActionCommand("female");
			femaleBtn.addActionListener(this);
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(maleBtn);
			buttonGroup.add(femaleBtn);
			gendalPan.add(maleBtn);
			gendalPan.add(femaleBtn);
			gendalPan.setMaximumSize(new Dimension(this.getWidth(), 0));
			box.add(gendalPan);

		}
		{// upload
			JPanel uploadPan = new JPanel(new CardLayout());
			uploadPan.setMaximumSize(new Dimension(this.getWidth(), 200));
			uploadPan.setPreferredSize(uploadPan.getMaximumSize());
			uploadPan.setBorder(BorderFactory.createTitledBorder("您现在的样子"));
			uploadPan.setBackground(Color.WHITE);
			JLabel uploadLab = new JLabel("点击选择你的头像,图片格式必须是png,gif,jpg");
			uploadLab.setHorizontalAlignment(JLabel.CENTER);
			uploadPan.add(uploadLab, "tips");
			this.nowPhotoIV = new ImageView();
			uploadPan.add(nowPhotoIV, "img");
			box.add(uploadPan);
			uploadPan.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					handleUploadPanClicked();
				}
			});
			this.nowPhotoPanel = uploadPan;
		}
		{// action
			JPanel actionPan = new JPanel();
			JButton actionBtn = new JButton("点击查看");
			actionBtn.setActionCommand("submit");
			JComboBox<String> yearCom = new JComboBox<String>(new String[] {
					"20", "30" });
			yearCom.setActionCommand("selectYear");
			yearCom.addActionListener(this);
			JLabel descLab = new JLabel("年后你的样子");
			actionPan.add(actionBtn);
			actionPan.add(yearCom);
			actionPan.add(descLab);
			actionPan.setMaximumSize(new Dimension(this.getWidth(), 100));
			box.add(actionPan);
			actionBtn.addActionListener(this);
			this.actionBtn = actionBtn;
			this.yearCbb = yearCom;
		}
		{// result
			JPanel resultPan = new JPanel(new CardLayout());
			resultPan.setBorder(BorderFactory.createTitledBorder("您将来的样子"));
			resultPan.setBackground(Color.WHITE);
			JLabel resultLab = new JLabel("结果显示在这");
			resultLab.setHorizontalAlignment(JLabel.CENTER);
			this.futurePhotoIV = new ImageView();
			{
				JProgressBar bar=new JProgressBar();
				bar.setStringPainted(true);
				bar.setMaximum(100);
				bar.setMinimum(0);
				bar.setBorderPainted(true);
				this.progressBar=bar;
			}
			
			resultPan.add(resultLab, "message");
			resultPan.add(this.progressBar, "progressBar");
			resultPan.add(this.futurePhotoIV, "img");
			box.add(resultPan);
			this.futurePhotoPanel = resultPan;
			this.messageLab = resultLab;
		}
		this.add(box);
	}

	private void showFileChoose() {
		if (this.imgChooser == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("F:/"));
			chooser.setDialogTitle("请选择你的头像图片");
			chooser.setAcceptAllFileFilterUsed(true);
			chooser.setFileFilter(new FileNameExtensionFilter(
					"图片(.png,.gif,.jpg)", "png", "jpg", "gif"));
			this.imgChooser = chooser;
		}
		this.imgChooser.showOpenDialog(this);
	}
	private void startProgress(){
		if(this.progressBarTimer==null){
			this.progressBarTimer=new Timer(500, this);
			this.progressBarTimer.setActionCommand("progress");
		}
		this.progressBar.setValue(0);
		this.progressBarTimer.restart();
	}
	public void handleUploadPanClicked() {
		showFileChoose();
		File file = this.imgChooser.getSelectedFile();
		if (file != null) {
			try {
				this.nowPhotoIV.setPath(file);
				switchCardLayoutView(nowPhotoPanel, "img");
				this.model.setPhoto(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		switch (evt.getActionCommand()) {
		case "progress":
			int currentValue=this.progressBar.getValue();
			if(currentValue>=99){
				this.progressBarTimer.stop();
				this.progressBar.setValue(99);
			}else{
				this.progressBar.setValue(currentValue+1);	
			}
			break;
		case "submit":
			this.handleSubmitAction((JButton) evt.getSource());
			break;
		case "selectYear":
			this.handleSelectYearAction((JComboBox) evt.getSource());
			break;
		case "male":
		case "female":
			this.handleSetGenderAction((JRadioButton) (evt.getSource()));
			break;
		}
	}

	private void handleSelectYearAction(JComboBox cbb) {
		String yearStr = (String) cbb.getSelectedItem();
		int year = Integer.parseInt(yearStr);
		this.model.setAgeingYear(year);
	}

	private void handleSubmitAction(JButton btn) {
		try {
			if (this.model.getPhoto() == null) {
				handleUploadPanClicked();
				return;
			}
			btn.setEnabled(false);
			this.switchCardLayoutView(futurePhotoPanel, "progressBar");
			startProgress();
			this.model.doAgeing(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleSetGenderAction(JRadioButton btn) {
		boolean isMale = "male".equals(btn.getActionCommand());
		this.model.setMale(isMale);
	}

	private void switchCardLayoutView(JPanel panel, String show) {
		LayoutManager layout = panel.getLayout();
		if (layout instanceof CardLayout) {
			((CardLayout) layout).show(panel, show);
		}
	}

	@Override
	public void done(final String url) {
		this.progress(100);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					actionBtn.setEnabled(true);
					futurePhotoIV.setPath(new URL(url));
					switchCardLayoutView(futurePhotoPanel, "img");
				} catch (Exception e) {
					error(e);
				} 

			}
		});
	}

	@Override
	public void error(final Exception e) {
		e.printStackTrace();
		actionBtn.setEnabled(true);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String message=e.getMessage();
				if(e instanceof UnknownHostException){
					message="网络问题,请检查网络";
				}
				messageLab.setText(message);
				switchCardLayoutView(futurePhotoPanel, "message");
			}
		});
	}


	@Override
	public void progress(final int value) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setValue(value);
			}
		});
	}
}
