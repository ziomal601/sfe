
import java.awt.Dimension;
import java.beans.FeatureDescriptor;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.sun.org.apache.bcel.internal.classfile.PMGClass;

public class ObjRecognitionController {
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

	private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

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
	private void startCamera() {
		getImage();
		// bind an image property with the original frame container
		final ObjectProperty<Image> imageProp = new SimpleObjectProperty<>();
		this.originalFrame.imageProperty().bind(imageProp);

		// set a fixed width for all the image to show and preserve image ratio
		this.imageViewProperties(this.originalFrame, 600);

		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(0);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				TimerTask frameGrabber = new TimerTask() {
					@Override
					public void run() {
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
			} else {
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");

			// stop the timer
			if (this.timer != null) {
				this.timer.cancel();
				this.timer = null;
			}
			// release the camera
			this.capture.release();
		}
	}

	private void getImage() {

		object = Highgui.imread("C:\\Users\\tomek\\workspace\\psw\\src\\door3.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		des_object = new Mat();
		this.detector = FeatureDetector.create(FeatureDetector.SURF);
		this.extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		keypoints_object = new MatOfKeyPoint();
		detector.detect(object, keypoints_object);
		extractor.compute(object, keypoints_object, des_object);
		points.add(new Point(0, 0));
		points.add(new Point(object.cols(), 0));
		points.add(new Point(object.cols(), object.rows()));
		points.add(new Point(0, object.rows()));
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame() {

		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// init
					Mat blurredImage = new Mat();
					Mat image = new Mat();
					// remove some noise
					// Imgproc.blur(frame, blurredImage, new Size(7, 7));

					// convert the frame to HSV
					Mat dst = new Mat();

					Imgproc.Canny(frame, image, 50, 200);
					Imgproc.cvtColor(image, dst, Imgproc.COLOR_GRAY2BGR);

					ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Mat hierarchy = new Mat();
					Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL,
							Imgproc.CHAIN_APPROX_SIMPLE);
					ArrayList<List<Point>> rect = new ArrayList<List<Point>>();
					System.out.println(contours.size());
					for (int i = 0; i < contours.size(); i++) {

						MatOfPoint2f approxCurve = new MatOfPoint2f();
						MatOfPoint2f point2f = new MatOfPoint2f(contours.get(i).toArray());

						System.out.println(
								point2f.cols() + " con" + contours.get(i).cols() + " cos: " + point2f.toList());

						Imgproc.approxPolyDP(point2f, approxCurve, Imgproc.arcLength(point2f, true) * 0.02, true);
						System.out.println("aprox: " + approxCurve.toList().size());

						MatOfPoint pointsq = new MatOfPoint(approxCurve.toArray());

						// Get bounding rect of contour
						Rect recta =Imgproc.boundingRect(pointsq);

						// draw enclosing rectangle (all same color, but you

						Core.rectangle(frame, new Point(recta.x, recta.y),
								new Point(recta.x + recta.width, recta.y + recta.height), new Scalar(255, 0, 0, 255),
								2);

						if (approxCurve.toList().size() == 4) {

							List<Point> points = new ArrayList<Point>();
							points = approxCurve.toList();
							double maxCosine = 0;
							for (int j = 2; j < 5; j++) {
								double cosine = Math
										.abs(angle(points.get(j % 4), points.get(j - 2), points.get(j - 1)));
								maxCosine = Math.max(maxCosine, cosine);
							}
							System.out.println("cosinus" + maxCosine);
							if (maxCosine < 0.3) {
								System.out.println(maxCosine);
								rect.add(points);
							}
						}

					}

					for (List<Point> matOfPoint : rect) {
						Core.line(frame, matOfPoint.get(0), matOfPoint.get(1), new Scalar(0, 255, 0), 3);
						Core.line(frame, matOfPoint.get(1), matOfPoint.get(2), new Scalar(0, 255, 0), 3);
						Core.line(frame, matOfPoint.get(2), matOfPoint.get(3), new Scalar(0, 255, 0), 3);
						Core.line(frame, matOfPoint.get(3), matOfPoint.get(0), new Scalar(0, 255, 0), 3);
					}

					//imageToShow = mat2Image(frame);

					/*
					 * Mat lines = new Mat(); int threshold = 50; int
					 * minLineSize = 50; int lineGap = 10;
					 * 
					 * Imgproc.HoughLinesP(image, lines, 1, Math.PI / 180,
					 * threshold, minLineSize, lineGap);
					 * 
					 * double[] data; double rho, theta; Point pt1 = new
					 * Point(); Point pt2 = new Point(); double a, b; double x0,
					 * y0;
					 * 
					 * ArrayList<Point> corners = new ArrayList<Point>(); for
					 * (int x = 0; x < lines.cols(); x++) { double[] vec =
					 * lines.get(0, x); double x1 = vec[0], y1 = vec[1], x2 =
					 * vec[2], y2 = vec[3]; Point start = new Point(x1, y1);
					 * Point end = new Point(x2, y2);
					 * 
					 * //Core.line(frame, start, end, new Scalar(255, 0, 0), 3);
					 * for (int j = x + 1; j < lines.cols(); j++) { double[]
					 * vecq = lines.get(0, j); Point pt = computeIntersect(vec,
					 * vecq); if (pt.x >= 0 && pt.y >= 0) corners.add(pt); } }
					 * for (Point point : corners) { Core.circle(frame, point,
					 * 5, new Scalar(0, 0, 255), 2); }
					 */
					/*
					 * Collections.sort(corners,new Comparator<Point>() {
					 * 
					 * @Override public int compare(Point o1, Point o2) { if
					 * (o1.x == o2.x) { return (int) (o1.y - o2.y); } else {
					 * return (int) (o1.x - o2.x); }
					 * 
					 * }
					 * 
					 * 
					 * });
					 */
					/*
					ArrayList<List<Point>> rect = new ArrayList<List<Point>>();
					List<Point> goodPoint = new ArrayList<Point>();
					if (corners.size() >= 4) {

						List<Point> points = corners;

						double maxCosine = 0;
						/*
						 * for (int j = 0; j < points.size() - 2; j++) { for
						 * (int k = 0; k < points.size(); k++) { for (int m = 0;
						 * m < points.size(); m++) { double cosine =
						 * Math.abs(angle(points.get(j), points.get(k),
						 * points.get(m))); maxCosine = Math.max(maxCosine,
						 * cosine); if (cosine < 0.3) {
						 * goodPoint.add(points.get(j));
						 * goodPoint.add(points.get(k));
						 * goodPoint.add(points.get(m)); } } } }
						 */
					/*
						System.out.println("cosinus" + maxCosine);

						if (maxCosine < 0.3) {
							System.out.println(maxCosine);
							rect.add(points);
						}
					}
					List<Point> realyGoodPoint = new ArrayList<Point>();
					for (int i = 0; i < goodPoint.size() - 1; i++) {
						if (!realyGoodPoint.contains(goodPoint.get(i))) {
							realyGoodPoint.add(goodPoint.get(i));
						}
					}
					for (int i = 0; i < realyGoodPoint.size() - 1; i++) {

						Core.line(frame, realyGoodPoint.get(i), realyGoodPoint.get(i + 1), new Scalar(0, 0, 255), 1);

					}
					*/
					// Core.line(frame, realyGoodPoint.get(realyGoodPoint.size()
					// - 1), realyGoodPoint.get(0), new Scalar(0, 0, 255), 1);

					/*
					 * for (List<Point> matOfPoint : rect) {
					 * 
					 * for (int i = 0; i < matOfPoint.size() - 1; i++) {
					 * 
					 * Core.line(frame, matOfPoint.get(i), matOfPoint.get(i +
					 * 1), new Scalar(0, 255, 0), 3);
					 * 
					 * }
					 * 
					 * }
					 */
					/*
					 * MatOfPoint2f approxCurve =new MatOfPoint2f();
					 * MatOfPoint2f cornersCopy =new MatOfPoint2f();
					 * cornersCopy.fromList(corners);
					 * Imgproc.approxPolyDP(cornersCopy,
					 * approxCurve,Imgproc.arcLength(cornersCopy,true)*0.02,
					 * true); if(approxCurve.cols() != 4){ System.out.println(
					 * "Not quadratic"); } else if(approxCurve.cols() == 4){
					 * System.out.println("Quadratic"); }
					 * 
					 * 
					 * System.out.println(approxCurve.cols());
					 */

					// Best one
					
					  int match_method = Imgproc.TM_CCOEFF_NORMED;
					  
					  Mat templ = Highgui.imread(
					  "C:\\Users\\tomek\\workspace\\psw\\src\\door.jpg");
					  
					  double minlocvalue = 7; double maxlocvalue = 7;
					  
					  double minminvalue = 7; double maxmaxvalue = 7;
					  
					  
					  //Create the result matrix 
					  int result_cols =
					  frame.cols() - templ.cols() + 1; int result_rows =
					  frame.rows() - templ.rows() + 1; Mat result = new
					  Mat(result_rows, result_cols, CvType.CV_32FC1);
					  
					  // / Do the Matching and Normalize
					  Imgproc.matchTemplate(frame, templ, result,
					  match_method); // 
					  Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
					  
					  // / Localizing the best match with minMaxLoc
					  MinMaxLocResult mmr = Core.minMaxLoc(result);
					  
					  System.out.println("min: "+mmr.minVal+" max: "
					  +mmr.maxVal);
					  
					  Point matchLoc; if (match_method == Imgproc.TM_SQDIFF ||
					  match_method == Imgproc.TM_SQDIFF_NORMED || match_method
					  == Imgproc.TM_CCOEFF_NORMED) { matchLoc = mmr.minLoc;
					  minminvalue = mmr.minVal; } else { matchLoc = mmr.maxLoc;
					  maxmaxvalue = mmr.minVal; }
					  
					  
					  if(mmr.maxVal > 0.55){ Core.rectangle(frame, matchLoc,
					  new Point(matchLoc.x + templ.cols(), matchLoc.y +
					  templ.rows()), new Scalar(0, 255, 0),3); }
					  
					 
					imageToShow = mat2Image(frame);

				}

			} catch (Exception e) {
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

	Double angle(Point pt1, Point pt2, Point pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	Point computeIntersect(double[] a, double[] b) {
		double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3];
		double x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];
		double d = ((double) (x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
		if (d > 0) {
			Point pt = new Point();
			pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
			pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
			return pt;
		} else
			return new Point(-1.0, -1.0);
	}

	private Mat findAndDrawBalls(Mat maskedImage, Mat frame) {
		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
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
	private void imageViewProperties(ImageView image, int dimension) {
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
	private Image mat2Image(Mat frame) {
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
	private <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				property.set(value);
			}
		});
	}

}
