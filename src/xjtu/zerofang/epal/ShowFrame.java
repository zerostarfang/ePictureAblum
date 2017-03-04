/*
* This class is used for
* @author starfang
* @version
* @time 2015-12-23 下午08:47:40
*/
package xjtu.zerofang.epal;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ShowFrame extends JFrame implements ActionListener,Runnable{
	
	public native int[] showPicture(int[] image1, int[] image2,int change);
	static{
		//System.loadLibrary(epalJNI);
	}
	
	private static final int DEFAULT_WIDTH = 1000;
	private static final int DEFAULT_HEIGHT = 700;
	
	private MenuBar myMenuBar;
	private Menu myMenu;
	private MenuItem addItem, delItem, startItem, clearItem, exitItem;
	private ArrayList<BufferedImage> myPhotos;
	private BufferedImage image1;
	private BufferedImage image2;
	private MyPanel myPanel;
	//private JPanel myPanel;
	private Runnable ra;
	
	//初始化窗体
	public ShowFrame(){
		super("我的电子相册");
		myPanel = new MyPanel();
		myMenuBar = new MenuBar();
		myMenu = new Menu("menu");
		addItem = new MenuItem("addPictures");
		delItem = new MenuItem("delPictures");
		startItem = new MenuItem("play");
		clearItem = new MenuItem("clearAll");
		exitItem = new MenuItem("exit");
		myPhotos = new ArrayList<BufferedImage>();
		
		addItem.addActionListener(this);
		delItem.addActionListener(this);
		startItem.addActionListener(this);
		clearItem.addActionListener(this);
		exitItem.addActionListener(this);
		
		myMenu.add(addItem);
		myMenu.addSeparator();
		myMenu.add(delItem);
		myMenu.addSeparator();
		myMenu.add(startItem);
		myMenu.addSeparator();
		myMenu.add(exitItem);
		myMenu.addSeparator();
		myMenu.add(clearItem);
		myMenu.addSeparator();
		
		myMenuBar.add(myMenu);
		
		setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
		this.setMenuBar(myMenuBar);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent w){
				System.exit(0);
			}	
		});
		
		
		setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
		add(myPanel);
		setVisible(true);
		
		//新建进程ra，开始sse4执行图片的转换
		/*
		ra = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int currentImage = myPhotos.size()-1;
				
				while(currentImage > 0){
					image1 = myPhotos.get(currentImage);
					image2 = myPhotos.get(--currentImage);
					
					int imageWidth = image1.getWidth();
					int imageHeight = image1.getHeight();
					
					int[] inPixels1 = new int[imageWidth*imageHeight];
					int[] inPixels2 = new int[imageWidth*imageHeight];
					
					getRGB(image1,0,0,imageWidth,imageHeight,inPixels1);
					getRGB(image2,0,0,imageWidth,imageHeight,inPixels2);
					
					long startTime = System.currentTimeMillis();
					for(int change = 0; change <= 255;change ++){
						inPixels1 = showPicture(inPixels1,inPixels2,change);
						setRGB(image1,0,0,imageWidth,imageHeight,inPixels1);
						myPanel.repaint();
					}
					long endTime = System.currentTimeMillis();
					System.out.println(endTime - startTime + "ms");
				}
			}	
		};
		*/
	}
	
	@Override
	public void actionPerformed(ActionEvent ae){
		//添加图片
		if(ae.getActionCommand() == "addPictures"){
			FileDialog newDialog = new FileDialog(this,"openFile",FileDialog.LOAD);
			newDialog.setDirectory(".");
			newDialog.setVisible(true);
			File myFile = new File(newDialog.getDirectory() + newDialog.getFile());
			try{
				image1 = ImageIO.read(myFile);
				myPanel.setPreferredSize(new Dimension(image1.getWidth(), image1.getHeight()));
				add(myPanel);
				pack();
				myPhotos.add(image1);
				
				myPanel.setSize(image1.getWidth(),image1.getHeight());
				myPanel.repaint();
			}catch (IOException ioe){
				ioe.printStackTrace();
			}
		}
		
		//开始执行效果
		if(ae.getActionCommand() == "play"){
			/**
			if(myPhotos.size() < 2){
				JOptionPane.showMessageDialog(null, "there's no enough pictures","提示",JOptionPane.INFORMATION_MESSAGE);
				return;
			}**/
			new Thread(this).start();
			
		}
		
		//退出
		if(ae.getActionCommand() == "exit"){
			System.exit(0);
		}
		
		//清空图片缓存
		if(ae.getActionCommand() == "clearAll"){		
			Graphics2D gd = (Graphics2D)(myPanel.getGraphics());
			gd.clearRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
			myPhotos.clear();
			image1.flush();
		}
	}
	
	//获取传入图片的像素值
	public void getRGB(BufferedImage img,int x, int y, int width, int height, int[] pixelsData){
		int type = img.getType();
		if(type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
			img.getRaster().getDataElements(x, y, width, width, pixelsData);
		}
		else{
			img.getRGB(x, y, width, height, pixelsData,0,img.getWidth());
		}
	}
	
	//设置传入图片的像素值
	public void setRGB(BufferedImage img,int x, int y, int width, int height, int[] pixelsData){
		int type = img.getType();
		if(type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
			img.getRaster().setDataElements(x, y, width, height, pixelsData);
		}
		else{
			img.setRGB(x, y, width, height, pixelsData,0,width);
		}
	}
	
	public int clamp(int value){
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}
	
	class MyPanel extends JPanel{
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g1 = (Graphics2D)g;
			this.setOpaque(false);
			g1.clearRect(0, 0, getWidth(), getHeight());
			g1.drawImage(image1, 0, 0, getWidth(), getHeight(), this);
		}
		
	}
	
	public static void main(String[] args){
		new ShowFrame();
	}

	@Override
	public void run() {
		int currentImage = myPhotos.size() - 1;
		while(currentImage > 0){
			image1 = myPhotos.get(currentImage);
			image2 = myPhotos.get(--currentImage);
			
			int imageWidth = image1.getWidth();
			
			int imageHeight = image1.getHeight();
			
			int[] inPixels1 = new int[imageWidth*imageHeight];
			int[] inPixels2 = new int[imageWidth*imageHeight];
			
			getRGB(image1,0,0,imageWidth,imageHeight,inPixels1);
			getRGB(image2,0,0,imageWidth,imageHeight,inPixels2);
			
			long startTime = System.currentTimeMillis();
			for(int fade = 0; fade < 255;fade++){
				//java实现两幅图渐变
				for(int row = 0; row < imageHeight; row++){
					int imgPix1 = 0, redPix1 = 0, greenPix1 = 0, bluePix1 = 0;
					int imgPix2 = 0, redPix2 = 0, greenPix2 = 0, bluePix2 = 0;
					int index = 0;
					
					for(int col = 0; col < imageWidth; col++){
						index = row * imageWidth + col;
						imgPix1 = (inPixels1[index] >> 24) & 0xff;
						redPix1 = (inPixels1[index] >> 16) & 0xff;
						greenPix1 = (inPixels1[index] >> 8) & 0xff;
						bluePix1 = inPixels1[index] & 0xff;
						
						imgPix2 = (inPixels1[index] >> 24) & 0xff;
						redPix2 = (inPixels1[index] >> 16) & 0xff;
						greenPix2 = (inPixels1[index] >> 8) & 0xff;
						bluePix2 = inPixels1[index] & 0xff;
						
						imgPix1 = (int) ((imgPix2 - imgPix1)*fade/255 + (float) imgPix1);
						redPix1 = (int) ((redPix2 - redPix1)*fade/255 + (float) redPix1);
						greenPix1 = (int) ((greenPix2 - greenPix1)*fade/255 + (float) greenPix1);
						bluePix1 = (int) ((bluePix2 - bluePix1)*fade/255 + (float) bluePix1);
						
						inPixels1[index] = (imgPix1 << 24)|(clamp(redPix1) << 16 )| (clamp(greenPix1) << 8 )| (clamp(bluePix1));
					}
				}
				setRGB(image1,0,0,imageWidth,imageHeight,inPixels1);
				myPanel.repaint();
			}
			long endTime = System.currentTimeMillis();
			System.out.println(endTime - startTime + "ms");
		}
	}
}
