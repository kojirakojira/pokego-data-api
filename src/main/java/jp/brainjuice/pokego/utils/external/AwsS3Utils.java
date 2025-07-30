package jp.brainjuice.pokego.utils.external;

import org.springframework.stereotype.Component;

@Component
public class AwsS3Utils {

//	@Value("${aws.s3.accessKey}")
//	private String accessKey;
//
//	@Value("${aws.s3.secretAccessKey}")
//	private String secretAccessKey;
//
//	@Value("${aws.s3.endpoint}")
//	@Getter
//	private String endpoint;
//
//	@Value("${aws.s3.s3BacketName}")
//	@Getter
//	private String s3BacketName;
//
//	/** 環境ごとのファイル名の接尾辞。システム固有の運用上の仕様 */
//	@Value("${aws.s3.suffix}")
//	@Getter
//	private String suffix;
//
//	public Resource download(String path) {
//
//		S3Client client = auth();
//
//		GetObjectRequest req = GetObjectRequest.builder()
//				.bucket(s3BacketName)
//				.key(path)
//				.build();
//		ResponseInputStream<GetObjectResponse> inputStream = client.getObject(req);
//		return new InputStreamResource(inputStream);
//	}
//
//	/**
//	 * AWS S3に保存されているファイルの一覧を取得します。<br>
//	 * 引数には"ディレクトリ"を相対パスで指定することにより、絞り込みが可能です。<br>
//	 * 例：「dir/」
//	 *
//	 * @param prefix
//	 * @return
//	 */
//	public List<S3Object> getImageList(String prefix) {
//
//		S3Client client = auth();
//
//		ListObjectsRequest listObjects = ListObjectsRequest.builder()
//				.bucket(s3BacketName)
//				.build();
//
//		ListObjectsResponse res = client.listObjects(listObjects);
//		List<S3Object> objList = res.contents();
//		return objList;
//	}
//
//	/**
//	 * 認証処理
//	 *
//	 * @return
//	 */
//	private S3Client auth() {
//
//		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
//		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
//
//		S3Client s3Client = S3Client.builder()
//				.region(Region.US_WEST_1)
//				.credentialsProvider(credentialsProvider)
//				.build();
//
//		return s3Client;
//	}
}
