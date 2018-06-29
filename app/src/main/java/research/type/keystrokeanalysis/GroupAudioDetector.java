package research.type.keystrokeanalysis;

import java.util.Arrays;

import jAudioFeatureExtractor.AudioFeatures.FeatureExtractor;
import jAudioFeatureExtractor.AudioFeatures.MFCC;
import jAudioFeatureExtractor.AudioFeatures.MagnitudeSpectrum;

//javac -classpath "/home/soumya/Type2Motion/jAudio-1.0.4.jar:/home/soumya/Type2Motion/jMusic1.6.5.jar:"  AudioProcessor.java
//java -classpath "/home/soumya/Type2Motion/jAudio-1.0.4.jar:/home/soumya/Type2Motion/jMusic1.6.5.jar:"  AudioProcessor
public class GroupAudioDetector{
	
	private static final int samplingRate=8000;
	private static final int fixedSampleSize=80000;

	public static boolean isGroupFunc(double[] args) {
		boolean isGroup=false;
		try {
			isGroup = detectGroup(args);
			System.out.println("Is Group: " + isGroup);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isGroup;
	}
	private static boolean detectGroup(double[] samples) throws Exception{



		for(int i=0;i<fixedSampleSize-samplingRate;i=i+samplingRate){
			System.out.println("Processing:");
			double[] seg_1=returnSegments(samples,i,i+samplingRate);
			double[] mfcc_1=mfcc(seg_1);
			double[] seg_2=returnSegments(samples,i+samplingRate,i+2*samplingRate);
			double[] mfcc_2=mfcc(seg_2);
			double sim=getCosineSimilarity(mfcc_1,mfcc_2);
			if(snr(seg_1)<=0.0 || snr(seg_2)<=0.0)
				continue;
			else{
				if(sim>Math.cos(Math.toDegrees(15))){
					System.out.println("Different");
					return true;
				}
			}
		}
		return false;
	}


	private static double[] returnSegments(double[] arr,int start,int end)
	{
		double[] segment=new double[samplingRate];
		int count=0;	
		for(int i=start;i<end;i++){
			segment[count]=arr[i];
			count+=1;
		}
		return segment;
	}
	private static double[] mfcc(double[] samples) throws Exception{
		FeatureExtractor fex=null;
		fex=new MagnitudeSpectrum();
		double[] mag=fex.extractFeature(samples,samplingRate,null);
		//System.out.println(mag.length);
		fex=null;
		fex=new MFCC();
		//System.out.println(Arrays.toString(fex.getDepenedencies()));
		//System.out.println(Arrays.toString(fex.getDepenedencyOffsets()));
		double[][] otherFeatures=new double[1][mag.length];
		otherFeatures[0]=mag;
		double[] melCeps=fex.extractFeature(samples,samplingRate,otherFeatures);
		System.out.println(Arrays.toString(melCeps));
		return melCeps;
	}
	private static double getCosineSimilarity(double[] vec1,double[] vec2){
		return (dotProduct(vec1,vec2)/(norm(vec1)*norm(vec2)));
	}
	private static double dotProduct(double[] vec1,double[] vec2){
		double sum=0.0;
		for(int i=0;i<vec1.length;i++)
			sum+=vec1[i]*vec2[i];
		return sum;
	}
	private static double norm(double[] vec){
		double sum=0.0;
		for(int i=0;i<vec.length;i++)
			sum+=Math.pow(vec[i],2.0);
		return(Math.sqrt(sum));
	}
	private static double snr(double[] sig){
		return (mean(sig)/std_dev(sig));
	}
	private static double mean(double[] sig){
		double sum=0.0;
		for(int i=0;i<sig.length;i++)
			sum+=sig[i];
		return (sum/sig.length);
	}
	private static double std_dev(double[] sig){
		double sum=0.0;
		double meanVal=mean(sig);
		for(int i=0;i<sig.length;i++)
			sum+=Math.pow((sig[i]-meanVal),2.0);
		return (Math.sqrt(sum/(sig.length-1)));
	}
}
