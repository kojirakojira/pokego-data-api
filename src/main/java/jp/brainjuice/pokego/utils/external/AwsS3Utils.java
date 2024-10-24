package jp.brainjuice.pokego.utils.external;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

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

	public Resource download(String path) {

		S3Client client = auth();

		GetObjectRequest req = GetObjectRequest.builder()
				.bucket(s3BacketName)
				.key(path)
				.build();
		ResponseInputStream<GetObjectResponse> inputStream = client.getObject(req);
		return new InputStreamResource(inputStream);
	}

	/**
	 * AWS S3に保存されているファイルの一覧を取得します。<br>
	 * 引数には"ディレクトリ"を相対パスで指定することにより、絞り込みが可能です。<br>
	 * 例：「dir/」
	 *
	 * @param prefix
	 * @return
	 */
	public List<S3Object> getImageList(String prefix) {

		S3Client client = auth();

		ListObjectsRequest listObjects = ListObjectsRequest.builder()
				.bucket(s3BacketName)
				.build();

		ListObjectsResponse res = client.listObjects(listObjects);
		List<S3Object> objList = res.contents();
		return objList;
	}

	/**
	 * 認証処理
	 *
	 * @return
	 */
	private S3Client auth() {

		String envAccessKeyId = System.getenv(accessKey);
		String envSecretAccessKey = System.getenv(secretAccessKey);
		AwsCredentials credentials = AwsBasicCredentials.create(envAccessKeyId, envSecretAccessKey);
		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

		S3Client s3Client = S3Client.builder()
				.region(Region.US_WEST_1)
				.credentialsProvider(credentialsProvider)
				.build();

		return s3Client;
	}
}
