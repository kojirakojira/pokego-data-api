package jp.brainjuice.pokego.utils.external;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import jp.brainjuice.pokego.utils.exception.FailedToSaveThumbnailException;
import lombok.Getter;

@Component
public class AwsS3Utils {

	@Value("${aws.s3.accessKey}")
	private String accessKey;

	@Value("${aws.s3.secretAccessKey}")
	private String secretAccessKey;

	@Value("${aws.s3.endpoint}")
	@Getter
	private String endpoint;

	@Value("${aws.s3.s3BacketName}")
	@Getter
	private String s3BacketName;

	/** 環境ごとのファイル名の接尾辞。システム固有の運用上の仕様 */
	@Value("${aws.s3.suffix}")
	@Getter
	private String suffix;

	/**
	 * AWS S3にファイルを保存します。<br>
	 * pathを指定する場合は、後ろに"/"を付けてください。
	 *
	 * @param multiFile
	 * @param path ファイルを保存するパス
	 * @return
	 * @throws FailedToSaveThumbnailException
	 */
//	@Async
	public String upload(MultipartFile multiFile, String path) throws FailedToSaveThumbnailException {

		if (multiFile == null) {
			return null;
		}

		AmazonS3 client = auth();

		path = path + multiFile.getOriginalFilename();

		try {
			// ヘッダにバイト数をセット
			ObjectMetadata om = new ObjectMetadata();
			om.setContentLength(multiFile.getBytes().length);

			final PutObjectRequest putRequest = new PutObjectRequest(s3BacketName, path, multiFile.getInputStream(), om);

			client.putObject(putRequest);

		} catch (Exception e) {
			throw new FailedToSaveThumbnailException(e);
		}

		return path;
	}

	public S3Object download(String path) {

		AmazonS3 client = auth();

		GetObjectRequest getReq = new GetObjectRequest(s3BacketName, path);
		S3Object object = client.getObject(getReq);
		return object;
	}

	/**
	 * AWS S3に保存されているファイルの一覧を取得します。<br>
	 * 引数には"ディレクトリ"を相対パスで指定することにより、絞り込みが可能です。<br>
	 * 例：「dir/」
	 *
	 * @param prefix
	 * @return
	 */
	public List<S3ObjectSummary> getImageList(String prefix) {

		AmazonS3 client = auth();

		ObjectListing objListing = client.listObjects(s3BacketName, prefix);
		List<S3ObjectSummary> objList = objListing.getObjectSummaries();

		return objList;
	}

	/**
	 * 認証処理
	 *
	 * @return
	 */
	private AmazonS3 auth() {

		String envAccessKeyId = System.getenv(accessKey);
		String envSecretAccessKey = System.getenv(secretAccessKey);
		AWSCredentials awsCreds = new BasicAWSCredentials(envAccessKeyId, envSecretAccessKey);

		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withRegion(Regions.US_WEST_1)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.build();

		return s3Client;
	}
}
