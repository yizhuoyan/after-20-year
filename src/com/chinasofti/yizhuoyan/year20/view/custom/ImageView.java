package com.chinasofti.yizhuoyan.year20.view.custom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageView extends javax.swing.JComponent {
	private static final long serialVersionUID = 2501387492499440180L;
	private Image image;
	private int imgWidth;
	private int imgHeight;
	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}
	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public void setPath(URL imgPath) throws IOException{
		this.setImage(ImageIO.read(imgPath));
	}
	public void setPath(String imgPath) throws IOException{
		this.setImage(ImageIO.read(new File(imgPath)));
	}
	@Override
	public Dimension getPreferredSize() {
		return this.getSize();
	}
	public void setPath(File imgPath) throws IOException{
		this.setImage(ImageIO.read(imgPath));
	}
	
	private void setImage(Image img) throws IOException{
		this.image=img;
		setImgWidth(image.getWidth(this));
		setImgHeight(image.getHeight(this));
		this.revalidate();
		this.repaint();
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(this.image==null)return;
		//获取组件获得的空间
		Dimension maxWH=this.getSize();
		int w=this.imgWidth;
		int h=this.imgHeight;
		int x = 0;
		int y = 0;
		if(w<maxWH.width){
			x=(maxWH.width-w)/2;
		}else{
			w=maxWH.width;
		}
		if(h<maxWH.height){
			y=(maxWH.height-h)/2;
		}else{
			h=maxWH.height;
		}
		g.drawImage(image, x, y, w,h,Color.WHITE,this);
	}
}
