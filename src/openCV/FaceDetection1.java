package openCV;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class FaceDetection1 {

	public static JFrame frame;
	static JLabel lbl;
	public static ImageIcon icon;
	public Point facePoint;

	public ImageIcon getIcon() {
		return icon;
	}

	public Point getCurrentFacePoint() {
		return facePoint;
	}

	public static void main(String[] args) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		CascadeClassifier cascadeFaceClassifier = new CascadeClassifier(
				"haarcascades/haarcascade_frontalface_default.xml");
		CascadeClassifier cascadeEyeClassifier = new CascadeClassifier("haarcascades/haarcascade_eye.xml");

		VideoCapture videoDevice = new VideoCapture();
		videoDevice.open(0);
		if (videoDevice.isOpened()) {

			while (true) {
				Thread.sleep(50);
				Mat frameCapture = new Mat();
				videoDevice.read(frameCapture);

				MatOfRect faces = new MatOfRect();
				cascadeFaceClassifier.detectMultiScale(frameCapture, faces);

				for (int i = 0; i < faces.toArray().length; i++) {
					Rect rect = faces.toArray()[i];
					Point facePoint = new Point(rect.x + rect.width, rect.y + rect.height);
//					FacialPoint fPoint = new FacialPoint(rect);
//					double area = rect.area();

					Imgproc.putText(frameCapture, "Face", new Point(rect.x, rect.y - 5), 1, 2, new Scalar(0, 0, 255));
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y),
							new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 100, 0), 3);

				}

				MatOfRect eyes = new MatOfRect();
				cascadeEyeClassifier.detectMultiScale(frameCapture, eyes);
				double[] eyeAreas = new double[eyes.toArray().length];
				int[] eyeYCoords = new int[5];
				for (int i = 0; i < eyeYCoords.length; i++) {
					eyeYCoords[i] = -1;
				}
				int countEye = 0;
				for (Rect rect : eyes.toArray()) {
					eyeAreas[countEye] = rect.area();
					eyeYCoords[countEye] = rect.y;
					countEye++;
					if (countEye > 2) {
						break;
					}
					Imgproc.putText(frameCapture, "Eye", new Point(rect.x, rect.y - 5), 1, 2, new Scalar(0, 0, 255));

					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y),
							new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(200, 200, 100), 2);
				}
				ArrayList<Double> areaDifferences = new ArrayList<>();
				int yDiff;
				if (eyeYCoords[0] != -1 & eyeYCoords[1] != -1) {
					yDiff = Math.abs(eyeYCoords[0] - eyeYCoords[1]);
				} else {
					yDiff = -1;
				}
				for (int i = 0; i < eyeAreas.length; i++) {
					for (int j = 0; j < eyeAreas.length; j++) {
						areaDifferences.add(Math.abs(eyeAreas[i] - eyeAreas[j]));

					}
				}
				double smallestDistance = -1;
				for (int i = 0; i < areaDifferences.size(); i++) {
					if (smallestDistance == -1 || areaDifferences.get(i) < smallestDistance) {
						if (areaDifferences.get(i) != 0) {
							smallestDistance = areaDifferences.get(i);
						}
					}
				}
//				System.out.println(yDiff);
				if (yDiff > 15 || smallestDistance > 700) {
					System.out.println("YOU ARE NOT FOCUSED");
				} 
//				System.out.println(smallestDistance);
				PushImage(ConvertMat2Image(frameCapture));
				System.out.println(
						String.format("%s FACES %s EYE detected.", faces.toArray().length, eyes.toArray().length));
			}
		} else {
			System.out.println("Video could not connect to the device.");
			return;
		}
	}

	private static BufferedImage ConvertMat2Image(Mat cameraData) {

		MatOfByte byteMatVerisi = new MatOfByte();

		Imgcodecs.imencode(".jpg", cameraData, byteMatVerisi);

		byte[] byteArray = byteMatVerisi.toArray();
		BufferedImage image = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			image = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return image;
	}

	public static void WindowPrepare() {
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void PushImage(Image img2) {

		if (frame == null)
			WindowPrepare();

		if (lbl != null)
			frame.remove(lbl);
		icon = new ImageIcon(img2);
		lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);

		frame.revalidate();
	}
}