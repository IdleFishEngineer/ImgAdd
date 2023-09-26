package com.demo.coo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final Boolean debug = true;

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
	// write your code here
        System.out.println("open cv version->"+Core.VERSION);

//        ADD_C("E:\\test_green\\em15_j_e2_a.png","E:\\test_green\\em15_j_e2_b.png","E:\\test_green\\em15_j_e2_b_1.png");

        ADD_A("E:\\test_alpha\\ek17_g_h4_a.png","E:\\test_alpha\\ek17_j_ha1_a.png","E:\\test_alpha\\ek17_j_ha1_a_1.png");
    }


    //Add two Images, second of them is clip, merge them into one picture
    public static void ADD_C(String filePath1,String filePath2,String filePath3) {
        Mat srcA = Imgcodecs.imread(filePath1);
        Mat srcB = Imgcodecs.imread(filePath2);

        if(srcA.empty() || srcB.empty()) {
//				  Dlog("athW's uroy rbomlep");
            if(debug) {
                Dlog(filePath1+" founded: "+!srcA.empty());
                Dlog(filePath2+" founded: "+!srcB.empty());
            }
            return;
        }else {
            if(debug) {
                Dlog("Images have been loaded!");
            }
        }

        Mat hsv = new Mat();
        Mat mask = new Mat();

        Imgproc.cvtColor(srcB, hsv, Imgproc.COLOR_BGR2HSV);	//*生成掩膜

        Core.inRange(hsv, new Scalar(35,43,46), new Scalar(77,255,255), mask);	//*绿色覆盖

        Core.bitwise_not(mask, mask);	//反掩

        Mat dst = new Mat();

        Core.bitwise_and(srcB,srcB,dst,mask); //黑底

        List<Mat> _lDstMat = new ArrayList<Mat>();

        Core.split(dst , _lDstMat);

        _lDstMat.add(mask);	//Mask Not

        Mat dstA = new Mat();
        Mat dstB = new Mat();

        Core.merge(_lDstMat, dstB);	//*透明 祛黑

        Imgproc.cvtColor(srcA, dstA, Imgproc.COLOR_BGR2BGRA);	//通道扩容

        Core.copyTo(dstB, dstA, mask);

        outDst(dstA,filePath3);		//*直接覆盖
    }

    //用原图的透明通道作为掩膜的图像融合方式，效果更好一些。
    public static void ADD_A(String filePath1,String filePath2,String filePath3) {
        Mat srcA = Imgcodecs.imread(filePath1,Imgcodecs.IMREAD_UNCHANGED);
        Mat srcB = Imgcodecs.imread(filePath2,Imgcodecs.IMREAD_UNCHANGED);	//$1 一定要以IMREAD_UNCHANGED模式读入，否则会忽视透明通道

        Mat cvtB = new Mat();
        Imgproc.cvtColor(srcB, cvtB, Imgproc.COLOR_BGR2BGRA);	//$2 将原图转换为带透明通道模式

        List<Mat> _lB = new ArrayList<Mat>();
        Core.split(cvtB,_lB);	//$3 将转换后的图片按通道分割

        if(_lB.size()!=4 || _lB.get(3).empty()) {
            Dlog("Unable to find alpha channel.");
            return;
        }
        Mat alpha = new Mat();

        List<Mat> _lAlp = new ArrayList<Mat>();

        _lAlp.add(_lB.get(3));	//$4 将透明通道单独提取出 并直接作为掩码

        Core.merge(_lAlp, alpha);

        Mat bin = new Mat();

        Imgproc.threshold(alpha, bin, 0, 255, Imgproc.THRESH_BINARY);

        Core.copyTo(srcB, srcA, bin);

        outDst(srcA,filePath3);
    }

    public static void outDst(Mat dst,String pathName) {
        if(new File(pathName).exists()) {
            throw new RuntimeException("File is exist!");
        }
        Imgcodecs.imwrite(pathName,dst);
    }

    public static final void Dlog(String message){
        System.out.println(message);
    }
}
