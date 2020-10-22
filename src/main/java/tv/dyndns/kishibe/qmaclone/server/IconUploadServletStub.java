//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class IconUploadServletStub extends HttpServlet implements Servlet {

  private static final Logger logger = Logger.getLogger(IconUploadServletStub.class.getName());
  private final Database database;

  @Inject
  public IconUploadServletStub(Database database) {
    this.database = database;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try (InputStream inputStream = request.getInputStream(); OutputStream outputStream = response.getOutputStream()) {
      processRequest(request, response);
    }
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    logger.log(Level.INFO, request.toString());

    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(64L * 1024L);
    List<FileItem> items = null;

    try {
      List<FileItem> temp = upload.parseRequest(request);
      items = temp;
    } catch (FileUploadException e) {
      writeResponse(response, Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_PARSE_REQUEST);
      return;
    }

    String userCode = null;
    BufferedImage inputImage = null;
    long originalFileSize = 0;

    for (FileItem item : items) {
      if (item.isFormField()) {
        String key = item.getFieldName();
        String value = item.getString();

        if (key.equals(Constant.FORM_NAME_USER_CODE)) {
          userCode = value;
        }

        continue;
      }

      // 画像確認
      originalFileSize = item.getSize();
      try (InputStream inputStream = item.getInputStream()) {
        inputImage = ImageIO.read(inputStream);
      } catch (IOException e) {
        writeResponse(response, Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_DETECT_IMAGE_FILE_TYPE);
        return;
      }

      if (inputImage == null) {
        writeResponse(response, Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_DETECT_IMAGE_FILE_TYPE);
        return;
      }
    }

    if (userCode == null || inputImage == null) {
      writeResponse(response, Constant.ICON_UPLOAD_RESPONSE_REQUEST_FORMAT_ERROR);
      return;
    }

    String extension = "jpg";
    String fileTitle = Utility.createFileName();
    String fileName = fileTitle + "." + extension;

    // リサイズ
    // TODO(nodchip): ユーティリティクラスへ抽出
    int size = Constant.ICON_SIZE * 2;
    ImageFilter imageFilter = new AreaAveragingScaleFilter(size, size);
    Image middleImage = new Canvas().createImage(new FilteredImageSource(inputImage.getSource(), imageFilter));
    BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    outputImage.createGraphics().drawImage(middleImage, 0, 0, size, size, null);

    // 一時ファイル書き込み
    new File(Constant.ICON_TEMP_FOLDER_PATH).mkdirs();
    File tempOutputFile = new File(Constant.ICON_TEMP_FOLDER_PATH, fileName);
    try {
      ImageIO.write(outputImage, "jpg", tempOutputFile);
    } catch (Exception e) {
      logger.log(Level.WARNING, String.format("画像ファイルの保存に失敗しました。 tempOutputFile=%s", tempOutputFile), e);
      throw new ServletException(e);
    }

    logger.log(Level.INFO, String.format("%d bytes -> %d bytes (%s)", originalFileSize, tempOutputFile.length(),
        tempOutputFile.getPath()));

    // webで公開しているフォルダへの配置
    new File(Constant.ICON_FOLDER_PATH).mkdirs();
    File outputFile = new File(Constant.ICON_FOLDER_PATH, fileName);
    try {
      tempOutputFile.renameTo(outputFile);
    } catch (Exception e) {
      logger.log(Level.WARNING, String.format("画像ファイルのwebで公開しているフォルダへの配置に失敗しました。 tempOutputFile=%s outputFile=%s",
          tempOutputFile, outputFile), e);
      throw new ServletException(e);
    }

    // データベース書き込み
    int userId = Integer.parseInt(userCode);
    PacketUserData userData;
    try {
      userData = database.getUserData(userId);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "ユーザーデータの読み込みに失敗しました", e);
      throw new ServletException(e);
    }
    File oldFile = new File(Constant.ICON_FOLDER_PATH, userData.imageFileName);
    if (oldFile.isFile() && !oldFile.getName().equals(Constant.ICON_NO_IMAGE)) {
      oldFile.delete();
    }
    userData.imageFileName = fileName;
    try {
      database.setUserData(userData);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "ユーザーデータの保存に失敗しました", e);
      throw new ServletException(e);
    }

    // レスポンス書き込み
    writeResponse(response, Constant.ICON_UPLOAD_RESPONSE_OK);
  }

  private void writeResponse(HttpServletResponse response, String message) throws IOException {
    response.setContentType("text/plain");
    response.getOutputStream().write(message.getBytes());
  }
}
