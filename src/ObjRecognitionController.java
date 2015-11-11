
import java.awt.Dimension;
import java.beans.FeatureDescriptor;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;


public class ObjRecognitionController
{
	// FXML camera button
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// the FXML area for showing the mask
	@FXML
	private ImageView maskImage;
	// the FXML area for showing the output of the morphological operations
	@FXML
	private ImageView morphImage;
	// FXML slider for setting HSV ranges
	@FXML
	private Slider hueStart;
	@FXML
	private Slider hueStop;
	@FXML
	private Slider saturationStart;
	@FXML
	private Slider saturationStop;
	@FXML
	private Slider valueStart;
	@FXML
	private Slider valueStop;
	// FXML label to show the current values set with the sliders
	@FXML
	private Label hsvCurrentValues;
	
	private FeatureDetector detector;
	
	private DescriptorExtractor extractor;
	
	private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED); 
	
	private Mat des_object = new Mat();
	
	private Mat des_image = new Mat();
	private MatOfKeyPoint keypoints_object;
	private Mat object;
	private List<Point> points = new ArrayList<>();
	// a timer for acquiring the video stream
	private Timer timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive;
	
	// property for object binding
	private ObjectProperty<Image> maskProp;
	private ObjectProperty<Image> morphProp;
	private ObjectProperty<String> hsvValuesProp;
	
	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	private void startCamera()
	{
		getImage();
		// bind an image property with the original frame container
		final ObjectProperty<Image> imageProp = new SimpleObjectProperty<>();
		this.originalFrame.imageProperty().bind(imageProp);
		
		
		// set a fixed width for all the image to show and preserve image ratio
		this.imageViewProperties(this.originalFrame, 600);
		
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(0);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				TimerTask frameGrabber = new TimerTask() {
					@Override
					public void run()
					{
						// update the image property => update the frame
						// shown in the UI
						Image frame = grabFrame();
						onFXThread(imageProp, frame);
					}
				};
				this.timer = new Timer();
				this.timer.schedule(frameGrabber, 0, 60);
				
				// update the button content
				this.cameraButton.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			
			// stop the timer
			if (this.timer != null)
			{
				this.timer.cancel();
				this.timer = null;
			}
			// release the camera
			this.capture.release();
		}
	}
	private void getImage(){
		
		object = Highgui.imread( "C:\\Users\\tomek\\workspace\\psw\\src\\karta.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE );

		des_object = new Mat();
		this.detector = FeatureDetector.create(4);
		this.extractor = DescriptorExtractor.create(2);
		keypoints_object =  new MatOfKeyPoint();
		detector.detect(object, keypoints_object);
		extractor.compute(object, keypoints_object, des_object);
		points.add(new Point(0,0));
		points.add(new Point( object.cols(), 0 ));
		points.add(new Point( object.cols(), object.rows() ));
		points.add(new Point( 0, object.rows() ));
	}
	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame()
	{

		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					// init
					Mat blurredImage = new Mat();
					Mat image = new Mat();
					// remove some noise
					Imgproc.blur(frame, blurredImage, new Size(7, 7));
					
					// convert the frame to HSV
					Imgproc.cvtColor(blurredImage, image, Imgproc.COLOR_RGBA2GRAY);
					MatOfKeyPoint keypoints_scene =  new MatOfKeyPoint();
					detector.detect(image, keypoints_scene);
					extractor.compute( image, keypoints_scene, des_image );
					MatOfDMatch matches = new MatOfDMatch();
					matcher.match(des_object, des_image,matches);
					// get thresholding values from the UI
					// remember: H ranges 0-180, S and V range 0-255
					
					
					
					List<DMatch> matchesList = matches.toList();
				    //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
					Double max_dist = 0.6;
				    Double min_dist = 100.0;
				    for(int i = 0; i < des_object.rows(); i++){
				        Double dist = (double) matchesList.get(i).distance;
				       
				        if(dist < min_dist) min_dist = dist;
				        if(dist > max_dist) max_dist = dist;
				    }
				    System.out.println(max_dist);
				    System.out.println(min_dist);
				    //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
				    LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
				    MatOfDMatch gm = new MatOfDMatch();
				    //good match = distance > 2*min_distance ==> put them in a list
				    for(int i = 0; i < des_object.rows(); i++){
				        if(matchesList.get(i).distance < 2* min_dist){
				            good_matches.addLast(matchesList.get(i));
				        }
				    }
				    //List -> Mat
				    gm.fromList(good_matches);

				    //-- Get the keypoints from the good matches
				    Mat img_matches = new Mat();
				    Features2d.drawMatches(
				    		object,
				            keypoints_object, 
				            image,
				            keypoints_scene, 
				            gm, 
				            img_matches);
				    System.out.println(good_matches.size());
				    if (good_matches.size() > 3){
					    //filter keypoints (use only good matches); First in a List, iterate, afterwards ==> Mat
					    LinkedList<Point> objList = new LinkedList<Point>();
					    LinkedList<Point> sceneList = new LinkedList<Point>();
					    List<KeyPoint> keypoints_objectList = keypoints_object.toList();
					    List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();
					    for(int i = 0; i<good_matches.size(); i++){
					        objList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
					        sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
					        /**objList.addLast(keypoints_objectList.get(good_matches.get(i).trainIdx).pt);
					        sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).queryIdx).pt);*/       
					    }
					    MatOfPoint2f obj = new MatOfPoint2f();
					    obj.fromList(objList);
					    MatOfPoint2f scene = new MatOfPoint2f();
					    scene.fromList(sceneList);
					    //calc transformation matrix; method = 8 (RANSAC) ransacReprojThreshold=3
					    Mat hg = Calib3d.findHomography(obj, scene,8,3);
					    //-- Get the corners from the image_1 ( the object to be "detected" )
					    Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
					    Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
					    //obj
					    obj_corners.put(0, 0, new double[] {0,0});
					    obj_corners.put(1, 0, new double[] {object.cols(),0});
					    obj_corners.put(2, 0, new double[] {object.cols(),object.rows()});
					    obj_corners.put(3, 0, new double[] {0,object.rows()});
					    //transform obj corners to scene_img (stored in scene_corners)
					    Core.perspectiveTransform(obj_corners,scene_corners, hg);
					     //move points for img_obg width to the right to fit the matching image
					    
					    Point p1 = new Point(scene_corners.get(0,0)[0]+object.cols(), scene_corners.get(0,0)[1]);
					    Point p2 = new Point(scene_corners.get(1,0)[0]+object.cols(), scene_corners.get(1,0)[1]);
					    Point p3 = new Point(scene_corners.get(2,0)[0]+object.cols(), scene_corners.get(2,0)[1]);
					    Point p4 = new Point(scene_corners.get(3,0)[0]+object.cols(), scene_corners.get(3,0)[1]);
					    //create the matching image
					    
					    System.out.println(p1);
					    System.out.println(p2);
					    System.out.println(p3);
					    System.out.println(p4);
					    System.out.println(image.height()+" w"+ image.width());
					    
					    Core.line(img_matches,  p1, p2,new Scalar(0, 255, 0),4);
					    Core.line(img_matches,  p2, p3,new Scalar(0, 255, 0),4);
					    Core.line(img_matches,  p3, p4,new Scalar(0, 255, 0),4);
					    Core.line(img_matches,  p4, p1,new Scalar(0, 255, 0),4);
					    
					    
					    //draw lines to the matching image
					    /*MatOfRect detections = new MatOfRect();
					    detections.diag(img_matches);    
					    for (Rect rect : detections.toArray()) {
					        System.out.println("running");
					        Core.rectangle(img_matches, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),new Scalar(0, 255, 0));
					    }*/
					   
						// find the tennis ball(s) contours and show them
						//frame = this.findAndDrawBalls(morphOutput, frame);
						
						// convert the Mat object (OpenCV) to Image (JavaFX)
				    }
					imageToShow = mat2Image(img_matches);
				}
				
			}
			catch (Exception e)
			{
				// log the (full) error
				System.err.print("ERROR");
				e.printStackTrace();
			}
		}
		
		return imageToShow;
	}
	
	/**
	 * Given a binary image containing one or more closed surfaces, use it as a
	 * mask to find and highlight the objects contours
	 * 
	 * @param maskedImage
	 *            the binary image to be used as a mask
	 * @param frame
	 *            the original {@link Mat} image to be used for drawing the
	 *            objects contours
	 * @return the {@link Mat} image with the objects contours framed
	 */
	private Mat findAndDrawBalls(Mat maskedImage, Mat frame)
	{
		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		
		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		
		
		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
		{
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			{
				MatOfPoint a = contours.get(0);
				Point[] points = a.toArray();
				for (Point point : points) {
					System.out.println(point.x + " ; " + point.y);
				}
				
				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
			}
		}
		
		return frame;
	}
	
	/**
	 * Set typical {@link ImageView} properties: a fixed width and the
	 * information to preserve the original image ration
	 * 
	 * @param image
	 *            the {@link ImageView} to use
	 * @param dimension
	 *            the width of the image to set
	 */
	private void imageViewProperties(ImageView image, int dimension)
	{
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}
	
	/**
	 * Convert a {@link Mat} object (OpenCV) in the corresponding {@link Image}
	 * for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame)
	{
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Highgui.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer

		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	
	
	/**
	 * Generic method for putting element running on a non-JavaFX thread on the
	 * JavaFX thread, to properly update the UI
	 * 
	 * @param property
	 *            a {@link ObjectProperty}
	 * @param value
	 *            the value to set for the given {@link ObjectProperty}
	 */
	private <T> void onFXThread(final ObjectProperty<T> property, final T value)
	{
		Platform.runLater(new Runnable() {
			
			@Override
			public void run()
			{
				property.set(value);
			}
		});
	}
	
}
