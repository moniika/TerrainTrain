package UI;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import algorithm.MapAnalysis;
import algorithm.MapUtil;
import algorithm.MapUtil.Pair;
import fileUtils.FileUtil;

public class TrainTerrainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//TODO Clean up code
	
	private JTabbedPane tabbedPane;
	
	private JLabel altitudeMap;
	private JLabel waterMap;
	private JLabel discreteMap;
	private JLabel accumulatedMap;
	private JLabel pathMap;
	
	private BufferedImage altitudeImage, waterImage, discreteImage, accumulatedImage, pathImage;

	private MapAnalysis analysis;

	private int[][] altitudeLayer;
	private int[][] waterLayer;

	public TrainTerrainPanel() {
		super(new BorderLayout());
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
				//nothing
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				//nothing
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				//nothing
			}
		});
		
		JPanel altitudePanel = new JPanel();
		altitudeMap = new JLabel();
		altitudePanel.add(altitudeMap);
		tabbedPane.addTab("Altitude Map", null, altitudePanel, null);
		
		JPanel waterPanel = new JPanel();
		waterMap = new JLabel();
		waterPanel.add(waterMap);
		tabbedPane.addTab("Water Map", null, waterPanel, null);
		
		JPanel discretePanel = new JPanel();
		discreteMap = new JLabel();
		discretePanel.add(discreteMap);
		tabbedPane.addTab("Discrete Map", null, discretePanel, null);
		
		JPanel accumulatedPanel = new JPanel();
		accumulatedMap = new JLabel();
		accumulatedPanel.add(accumulatedMap);
		tabbedPane.addTab("Accumulated Cost Map", null, accumulatedPanel, null);
		
		JPanel pathPanel = new JPanel();
		pathMap = new JLabel();
		pathPanel.add(pathMap);
		tabbedPane.addTab("Calculated Path", null, pathPanel, null);
		
		
		JPanel inputAndOptionsPanel = new JPanel();
		add(inputAndOptionsPanel, BorderLayout.PAGE_END);
		
		JPanel weightingPanel = new JPanel();
		inputAndOptionsPanel.add(weightingPanel);
		
		
		
		JPanel buttonPanel =  new JPanel();
		inputAndOptionsPanel.add(buttonPanel);
		
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
		
		final JButton altitudeMapButton = new JButton("Altitude Map");
		buttonPanel.add(altitudeMapButton);
		
		altitudeMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						altitudeImage = ImageIO.read(fileChooser.getSelectedFile());
						altitudeLayer = FileUtil.imageToMap(altitudeImage);
						altitudeMap.setIcon(new ImageIcon(altitudeImage));
					} catch (IOException e) {
						altitudeImage = null;
						altitudeLayer = null;
					}
					updateImages();					
				}

			}
		});
		
		final JButton waterMapButton = new JButton("Water Map");
		buttonPanel.add(waterMapButton);
		
		waterMapButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(TrainTerrainPanel.this);

				// If file was chosen, update contents of htmlPane
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clearAnalysisImages();
					try {
						waterImage = ImageIO.read(fileChooser.getSelectedFile());
						waterLayer = FileUtil.imageToMap(waterImage);
						waterMap.setIcon(new ImageIcon(waterImage));
					} catch (IOException e) {
						waterImage = null;
						waterLayer = null;
					}
					updateImages();
				}

			}
		});
		
		
		final JButton analysisButton = new JButton("Perform Analysis");
		buttonPanel.add(analysisButton);
		
		analysisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(altitudeLayer == null) { // If an altitude map has not been set
					JOptionPane.showMessageDialog(null, "Altitude Map is not Set");
				} else if(waterLayer != null && (altitudeLayer.length != waterLayer.length || altitudeLayer[0].length != waterLayer[0].length)) {
					JOptionPane.showMessageDialog(null, "Altitude and Water Map dimensions do not match");
				} else {
					// Set mapping of map type to map data
					Map<MapUtil.MapTypes, int[][]> layers = new HashMap<MapUtil.MapTypes, int[][]>();
					layers.put(MapUtil.MapTypes.ALTITUDE, invertGraph(altitudeLayer));
					if(waterLayer != null) {
						layers.put(MapUtil.MapTypes.WATER, invertGraph(waterLayer));
					}
					
					// Set source for path
					int[][] source = new int[altitudeLayer.length][altitudeLayer[0].length];
					source[source.length - 1][source[0].length - 1] = 1;
					// Set start for path
					Pair<Integer, Integer> start = new Pair<Integer, Integer>(0, 0);
					
					//TODO modify this to be updated in the UI
					//TODO also make sure that each MapType in layers has a weighting (otherwise an exception is thrown in MapAnalysis)
					Map<MapUtil.MapTypes, Double> weightings = new HashMap<MapUtil.MapTypes, Double>();
					weightings.put(MapUtil.MapTypes.ALTITUDE, 1.0);
					weightings.put(MapUtil.MapTypes.WATER, 2.0);
					
					// Perform analysis
					analysis = new MapAnalysis(source, start, layers, weightings);
					
					// Update Images
					discreteImage = discreteCostMapToBufferedImage(analysis.discreteCost);
					accumulatedImage = accumulatedCostMapToBufferedImage(analysis.accumulatedCost);
					pathImage = pathAndAltitudeToBufferedImage(analysis.path, altitudeLayer);
					updateImages();
				}
			}
		});
		
		final JButton resetButton = new JButton("Reset");
		buttonPanel.add(resetButton);
		
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Clear altitudeLayer
				altitudeLayer = null;
				// Clear waterLayer
				waterLayer = null;
				
				// Clear Images
				altitudeImage = waterImage = discreteImage = accumulatedImage = pathImage = null;
				updateImages();
			}
		});

	}
	
	private int[][] invertGraph(int[][] graph) {
		int[][] inverted = new int[graph.length][graph[0].length];
		for(int i = 0; i < graph.length; i++) {
			for(int j = 0; j < graph[0].length; j++) {
				inverted[i][j] = 255 - graph[i][j]; 
			}
		}
		return inverted;
	}
	
	private void clearAnalysisImages() {
		discreteImage = accumulatedImage = pathImage = null;
	}
	
	/**
	 * updates displayed images, scaled to fit window
	 */
	private void updateImages() {
		//find size of our images, if possible
		int imageWidth, imageHeight;
		if (altitudeImage != null) {
			imageWidth = altitudeImage.getWidth();
			imageHeight = altitudeImage.getHeight();
		} else if (waterImage != null) {
			imageWidth = waterImage.getWidth();
			imageHeight = waterImage.getHeight();
		} else {
			altitudeMap.setIcon(null);
			waterMap.setIcon(null);
			discreteMap.setIcon(null);
			accumulatedMap.setIcon(null);
			pathMap.setIcon(null);
			return;
		}
		int paneWidth = tabbedPane.getWidth();
		int paneHeight = tabbedPane.getHeight();
		//calculate desired display dimensions
		int width, height;
		if (paneWidth/paneHeight < imageWidth/imageHeight) { //comparing aspect ratios
			//match pane width
			width = paneWidth;
			height = paneWidth * imageHeight / imageWidth;
		} else {
			//match pane height
			width = paneHeight * imageWidth / imageHeight;
			height = paneHeight;
		}
		
		updateImage(altitudeMap, altitudeImage, width, height);
		updateImage(waterMap, waterImage, width, height);
		updateImage(discreteMap, discreteImage, width, height);
		updateImage(accumulatedMap, accumulatedImage, width, height);
		updateImage(pathMap, pathImage, width, height);
	}
	private void updateImage(JLabel label, BufferedImage image, int width, int height) {
		if (image != null) {
			label.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_DEFAULT)));
		} else {
			label.setIcon(null);
		}
	}
	
	private BufferedImage discreteCostMapToBufferedImage(double[][] discreteCost) {
		int[][] modifiedMap = new int[discreteCost.length][discreteCost[0].length];
		double max = 1;
		for(int i = 0; i < discreteCost.length; i++) {
			for(int j = 0; j < discreteCost[0].length; j++) {
				if(discreteCost[i][j] > max) max = discreteCost[i][j]; 
			}
		}
		for(int i = 0; i < discreteCost.length; i++) {
			for(int j = 0; j < discreteCost[0].length; j++) {
				modifiedMap[i][j] = (int)(discreteCost[i][j] * 255/max);
			}
		}
		return (BufferedImage) FileUtil.mapToImage(modifiedMap);
	}
	
	private BufferedImage accumulatedCostMapToBufferedImage(double[][] accumulatedCost) {
		int[][] modifiedMap = new int[accumulatedCost.length][accumulatedCost[0].length];
		double max = 1;
		for(int i = 0; i < accumulatedCost.length; i++) {
			for(int j = 0; j < accumulatedCost[0].length; j++) {
				if(accumulatedCost[i][j] > max) max = accumulatedCost[i][j]; 
			}
		}
		for(int i = 0; i < modifiedMap.length; i++) {
			for(int j = 0; j < modifiedMap[0].length; j++) {
				modifiedMap[i][j] = (int)(accumulatedCost[i][j]*255/max);
			}
		}
		return (BufferedImage) FileUtil.mapToImage(modifiedMap);
	}
	
	private BufferedImage pathAndAltitudeToBufferedImage(int[][] path, int[][] altitudeMap) {
		BufferedImage pathImage = FileUtil.mapToImage(altitudeMap);
		for(int i = 0; i < path.length; i++) {
			for(int j = 0; j < path[0].length; j++) {
				if(path[i][j] == 1) {
					pathImage.setRGB(i, j, 255<<16); //red
				}
			}
		}
		return pathImage;
	}
}
