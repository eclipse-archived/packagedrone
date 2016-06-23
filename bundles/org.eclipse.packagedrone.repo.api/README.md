# Upload API V3

The base URL for this endpoint is `/api/v3/upload`. It is recommended to either use
it only in a local network use enforce the use of HTTPS.

## Authentication

The upload mechanism uses HTTP basic authentication using an arbitrary
user name (historically `deploy`) and the deploy key token. This requires to create
and assign at least one deploy group with at least one deploy key to the channel.

## Uploading

There are two ways of uploading artifacts with the new API. Either a single file or
a complete archive.

### Single upload

Endpoint: `/api/v3/upload/plain/${target}/${name}`

Also see "Targeting" below.

In order to upload an artifact issue a `PUT` or `POST` request to the endpoint, with the request body being the actual binary data which should be uploaded.
The URL path after the `${target}` part will be considered the name of the artifact. It may contains slashes.

All query parameters which are provided will be converted to meta keys and must have the key format of `namespace:key`. Those keys will be assigned as
provided meta data and stored with the artifact.

The following command will use `curl` to upload the local file `README.md` as `FOO.MD` to the channel `channel-id`: 

    curl -X PUT --data-binary @README.md http://deploy:583ef5a61ec1694cf0827c2de39b59b4eec222e44f983c2c064b3586e4e65637@localhost:8080/api/v3/upload/plain/channel/channel-id/FOO.md?test:foo=bar

### Archive upload

With the archive upload, if one artifact of the archive fails to be created, the whole
request is being rolled back.

Also see "Targeting" below.

Endpoint: `/api/v3/upload/archive/${target}`

Uploading an artifact works similar to uploading a single file.
Only are the names and meta data properties stored inside the
archive together with the actual binary data.

The upload archive is actually a ZIP file with a defined format.
The following example shows a simple structure of two artifacts `art1` and `art1.1`,
which is a child of `art1`: 

	/artifacts/art1/content                       -> binary data
	/artifacts/art1/artifacts/art1.1/content      -> binary data

The name can be overridden by adding an entry `name` in this hierarchy. Thw following
example will override the name of the `art1.1` artifact to be `art1/art1.1`. This is
optional, but required in the case the artifact name contains a slash (/) since this would
then alter the directory structure in the ZIP file.

	/artifacts/art1/content                       -> binary data
	/artifacts/art1/artifacts/art1.1/name         -> "art1/art1.1"
	/artifacts/art1/artifacts/art1.1/content      -> binary data

It is also possible to add meta data properties for each file:

	/artifacts/art1/properties.json               -> "{'foo:bar': 'value'}"
	/artifacts/art1/content                       -> binary data
	/artifacts/art1/artifacts/art1.1/content      -> binary data
	
**Note: ** The order of the entries in the ZIP is is _important_! Parents must be
added before their children and the `content` entry must follow the `name` and `properties.json` entry.

## Targeting

The `${target}` part of the request past consists of the target type and the actual target identifier.
It is possible to upload files to a channel, as root artifacts, or as children to an artifact in a channel.

The syntax of the target part is: `${type}/${id}`. Depending on the type the id is:

<dl>
<dt>channel</dt>
<dd>The channel id or name</dd>
<dt>artifact</dt>
<dd>The channel id or name followed by the artifact id: `some-channel/63f35714-392b-11e6-8751-43d7c67f7df5`</dd>
</dl>

### Examples

Uploading a single file to a channel called "foo" with the name "myfile" would be:

	/api/v3/upload/plain/channel/foo/myfile
	
Uploading a single file beneath the artifact `63f35714-392b-11e6-8751-43d7c67f7df5` in the channel `some-channel` would be:

	/api/v3/upload/plain/artifact/some-channel/63f35714-392b-11e6-8751-43d7c67f7df5/myfile

## Result

For a successful HTTP request (200) the result will always be a JSON encoded message of the
type [`UploadResult`](src/org/eclipse/packagedrone/repo/api/upload/UploadResult.java).

For an unsuccessful HTTP request (4xx, 5xx) the result will always be a JSON encoded message of the type [`UploadError`](src/org/eclipse/packagedrone/repo/api/upload/UploadError.java).

***Note: ** Even a successful upload may have chosen to reject artifacts, but not to fail the process. This can e.g. be configured using the trigger system. In this case the upload request
will be successful and the `UploadResult` structure will contain more information in the
`rejectedArtifacts` list. 