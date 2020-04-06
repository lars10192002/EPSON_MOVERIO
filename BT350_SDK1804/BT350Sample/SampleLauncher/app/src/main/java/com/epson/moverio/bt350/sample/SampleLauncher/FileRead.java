/*
 * Copyright(C) Seiko Epson Corporation 2016. All rights reserved.
 *
 * Warranty Disclaimers.
 * You acknowledge and agree that the use of the software is at your own risk.
 * The software is provided "as is" and without any warranty of any kind.
 * Epson and its licensors do not and cannot warrant the performance or results
 * you may obtain by using the software.
 * Epson and its licensors make no warranties, express or implied, as to non-infringement,
 * merchantability or fitness for any particular purpose.
 */

package com.epson.moverio.bt350.sample.SampleLauncher;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class FileRead {

    Context mContext;
    BufferedInputStream in = null;
    private String m_filename;
    private String mApkName;
    private String mPackageName;

    FileRead(String filename) {
        m_filename = filename;
        try {
            readTextFromFile(m_filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  void readTextFromFile(String path) throws IOException {

        try {
            FileReader in = new FileReader(m_filename);
            BufferedReader br = new BufferedReader(in);
            String line;
            if((line = br.readLine()) != null) {
                mApkName = line;
            }
            if((line = br.readLine()) != null) {
                mPackageName = line;
            }
            br.close();
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public String getApkName(){
        return mApkName;
    }

    public String getPackageName(){
        return mPackageName;
    }
}

