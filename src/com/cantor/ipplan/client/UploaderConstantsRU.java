package com.cantor.ipplan.client;

import gwtupload.client.IUploader.UploaderConstants;

public interface UploaderConstantsRU extends UploaderConstants {
	
    @DefaultStringValue(" ")
    String uploadLabelCancel();
    @DefaultStringValue("Отменить")
    String uploadStatusCanceled();
    @DefaultStringValue("Отмена ...")
    String uploadStatusCanceling();
    @DefaultStringValue("Удалить")
    String uploadStatusDeleted();
    @DefaultStringValue("Ошибка")
    String uploadStatusError();
    @DefaultStringValue("В процессе")
    String uploadStatusInProgress();
    @DefaultStringValue("В очереди")
    String uploadStatusQueued();
    @DefaultStringValue("Отправляю ...")
    String uploadStatusSubmitting();
    @DefaultStringValue("Далее")
    String uploadStatusSuccess();

    @DefaultStringValue("Загрузка уже активирована. Попробуйте позже.")
    String uploaderActiveUpload();

    @DefaultStringValue("Этот файл уже загружен.")
    String uploaderAlreadyDone();

    @DefaultStringValue("It seems the application is configured to use GAE blobstore.\nThe server has raised an error while creating an Upload-Url\nBe sure thar you have enabled billing for this application in order to use blobstore.")
    String uploaderBlobstoreError();

    @DefaultStringValue("Выбрать файл для загрузки...")
    String uploaderBrowse();

    @DefaultStringValue("Неверный тип файла.\nДопустимы только следующие типы:\n")
    String uploaderInvalidExtension();

    @DefaultStringValue("Отправить")
    String uploaderSend();

    @DefaultStringValue("Неверный ответ сервера. Приложение неверно настроено на стороне сервера.")
    String uploaderServerError();
    
    @DefaultStringValue("Не могу отправить файл из-за запрета установленного в браузере.")
    String submitError();
    
    @DefaultStringValue("Не удается связаться с сервером: ")
    String uploaderServerUnavailable();

    @DefaultStringValue("Истекло время для отправки файла.\nВозможно, ваш браузер не отправлять файлы правильно,\nваша сессия истекла или была ошибка сервера.\nПожалуйста, попробуйте еще раз.")
    String uploaderTimeout();
    
    @DefaultStringValue("Ошибка загрузки файла. Сервер прислал неизвестный ответ.\n.")
    String uploaderBadServerResponse();

    @DefaultStringValue("Вы ввели неправильное имя файла. Сделайте его допустимым.")
    String uploaderInvalidPathError();
}
