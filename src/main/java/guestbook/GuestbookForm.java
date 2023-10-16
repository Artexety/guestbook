/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guestbook;

import jakarta.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Type to bind request payloads and make them available in the controller. In contrast to {@link GuestbookEntry} it is
 * particularly designed to also be able to capture invalid input, so that the raw form data can be bound and validated
 * against business constraints using code and / or annotations.
 * <p>
 * Note how the fields are annotated with the {@link NotBlank} annotation, which tells Spring how to validate the values.
 *
 * @author Oliver Drotbohm
 * @see GuestbookController#addEntry(GuestbookForm, org.springframework.validation.Errors, org.springframework.ui.Model)
 */
class GuestbookForm {

	private final @NotBlank String name;
	private final @NotBlank String text;
	private final @NotBlank String mail;

    private MultipartFile image;
    private static String UPLOAD_DIR = "uploads/";

	/**
	 * Creates a new {@link GuestbookForm} with the given name and text. Spring Framework will use this constructor to
	 * bind the values provided in the web form described in {@code src/main/resources/templates/guestbook.html}, in
	 * particular the {@code name} and {@code text} fields as they correspond to the parameter names of the constructor.
	 * The constructor needs to be public so that Spring will actually consider it for form data binding until
	 * {@link https://github.com/spring-projects/spring-framework/issues/22600} is resolved.
	 *
	 * @param name the value to bind to {@code name}
	 * @param text the value to bind to {@code text}
	 */
	public GuestbookForm(String name, String text, String mail, MultipartFile image) {

        this.image = image;

		this.name = name;
		this.text = text;
		this.mail = mail;
	}

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }


	/**
	 * Returns the value bound to the {@code name} attribute of the request. Needs to be public so that Spring will
	 * actually consider it for form data binding until
	 * {@link https://github.com/spring-projects/spring-framework/issues/22600} is resolved.
	 *
	 * @return the value bound to {@code name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value bound to the {@code text} attribute of the request. Needs to be public so that Spring will
	 * actually consider it for form data binding until
	 * {@link https://github.com/spring-projects/spring-framework/issues/22600} is resolved.
	 *
	 * @return the value bound to {@code text}
	 */
	public String getText() {
		return text;
	}


	/**
	 * as above but for the mail field
	 *
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * Returns a new {@link GuestbookEntry} using the data submitted in the request.
	 *
	 * @return the newly created {@link GuestbookEntry}
	 * @throws IllegalArgumentException if you call this on an instance without the name and text actually set.
	 */
	GuestbookEntry toNewEntry() {
        String imageUrl = saveImageAndGetUrl(this.image);
		return new GuestbookEntry(getName(), getText(), getMail(), imageUrl);
	}

    private String saveImageAndGetUrl(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            // handle case where no image was provided
            return null;
        }

        try {
            byte[] bytes = image.getBytes();
            Path path = Paths.get(UPLOAD_DIR + image.getOriginalFilename());
            Files.write(path, bytes);

            // Here, you'd return the URL that points to the new image.
            // This assumes that there's a static file server serving the upload directory
            // under the /uploads/ path.
            return "/uploads/" + image.getOriginalFilename();

        } catch (IOException e) {
            // handle exception (you might want to throw it so that the caller can handle it)
            e.printStackTrace();
            return null;
        }
    }
}
