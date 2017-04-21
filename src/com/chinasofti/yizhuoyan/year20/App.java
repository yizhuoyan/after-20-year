package com.chinasofti.yizhuoyan.year20;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.chinasofti.yizhuoyan.year20.model.AppModel;
import com.chinasofti.yizhuoyan.year20.model.impl.HttpClientImpl;
import com.chinasofti.yizhuoyan.year20.model.impl.HttpURLConnectionImpl;
import com.chinasofti.yizhuoyan.year20.view.AppWindow;

public class App {
	public static void main(String[] args) throws Exception {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	        if ("Nimbus".equals(info.getName())) {
	            UIManager.setLookAndFeel(info.getClassName());
	            break;
	        }
	    }
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try{
				AppModel model = new HttpClientImpl();
				AppWindow win = new AppWindow(model);
				win.setLocationRelativeTo(null);
				win.setVisible(true);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});

	}
}
