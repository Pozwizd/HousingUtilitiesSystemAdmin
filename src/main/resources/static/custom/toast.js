
/**
 * ==========================================================================================
 * Notyf - Конфигурация и инициализация
 * ==========================================================================================
 */

// Расширяем класс Notyf, чтобы разрешить использование HTML в сообщениях
class CustomNotyf extends Notyf {
    _renderNotification(options) {
        const notification = super._renderNotification(options);
        // Заменяем textContent на innerHTML для рендеринга HTML
        if (options.message) {
            const messageContainer = notification.querySelector('.notyf__message');
            if (messageContainer) {
                messageContainer.innerHTML = options.message;
            }
        }
        return notification;
    }
}


// Создаем и настраиваем экземпляр Notyf
// Замените config.colors на ваши реальные значения цветов, если они есть,
// или оставьте стандартные цвета Notyf.
const notyf = new CustomNotyf({
    duration: 5000,       // Длительность показа в миллисекундах
    ripple: true,         // Эффект "волны" при клике
    dismissible: true,    // Можно ли закрыть уведомление
    position: { x: 'right', y: 'top' }, // Позиция по умолчанию
    types: [
        {
            type: 'info',
            // background: config.colors.info, // Пример с кастомными цветами
            backgroundColor: '#2b9af3',
            icon: {
                className: 'ti tabler-info-circle',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'warning',
            // background: config.colors.warning,
            backgroundColor: '#ff9f43',
            icon: {
                className: 'ti tabler-alert-triangle',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'success',
            // background: config.colors.success,
            backgroundColor: '#7367f0',
            icon: {
                className: 'ti tabler-circle-check',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'error',
            // background: config.colors.danger,
            backgroundColor: '#ea5455',
            icon: {
                className: 'ti tabler-alert-circle',
                tagName: 'i',
                color: 'white'
            }
        }
    ]
});

/**
 * ==========================================================================================
 * Основные функции для отображения уведомлений
 * ==========================================================================================
 */

/**
 * Отображает уведомление Notyf с возможностью перевода и интерактивными элементами.
 * @param {string} type - Тип уведомления ('success', 'error', 'warning', 'info').
 * @param {string | null} title - Заголовок уведомления. Может быть ключом для i18next.
 * @param {string} msg - Сообщение уведомления. Может быть ключом для i18next или содержать HTML.
 * @param {boolean} [isI18n=false] - Флаг, указывающий на необходимость перевода title и msg через i18next.
 * @param {object} [options={}] - Дополнительные опции для Notyf (например, duration, dismissible).
 */
function showToast(type, title, msg, isI18n = false, options = {}) {
    let translatedTitle = title;
    let translatedMsg = msg;

    // Если передан флаг isI18n, используем i18next для перевода
    if (isI18n && typeof i18next !== 'undefined') {
        if (translatedTitle && translatedTitle.trim() !== '') {
            translatedTitle = i18next.t(translatedTitle);
        }
        if (translatedMsg && translatedMsg.trim() !== '') {
            translatedMsg = i18next.t(translatedMsg);
        }
    }

    // Объединяем заголовок и сообщение в один HTML-блок
    let finalMessage = '';
    if (translatedTitle && translatedTitle.trim() !== '') {
        finalMessage += `<strong>${translatedTitle}</strong><br>`;
    }
    finalMessage += translatedMsg;

    // Собираем опции для уведомления
    const notificationOptions = {
        type: type,
        message: finalMessage,
        ...options // Позволяет переопределить любые стандартные опции
    };

    // Отображаем уведомление и получаем его экземпляр для дальнейшей работы
    const notification = notyf.open(notificationOptions);

    // Если в сообщении есть интерактивные элементы, находим их и назначаем обработчики
    // Этот подход гораздо надежнее, чем setTimeout и getElementById
    if (notification && notification.dom) {
        const okBtn = notification.dom.querySelector('#okBtn');
        if (okBtn) {
            okBtn.addEventListener('click', () => {
                // Закрываем конкретное это уведомление
                notyf.dismiss(notification);
            });
        }

        const surpriseBtn = notification.dom.querySelector('#surpriseBtn');
        if (surpriseBtn) {
            surpriseBtn.addEventListener('click', () => {
                alert('Surprise! You could perform an action here.');
                // Можно показать другое уведомление в ответ
                showToast('success', 'Surprise!', 'This is a new message from the button.');
            });
        }
    }
}

/**
 * Удобная обертка для показа уведомлений с автоматическим переводом ключей i18next.
 * @param {string} shortCutFunction - Тип уведомления ('success', 'error', 'warning', 'info').
 * @param {string} titleKey - Ключ i18next для заголовка.
 * @param {string} msgKey - Ключ i18next для сообщения.
 * @param {object} [options={}] - Дополнительные опции для Notyf.
 */
function showI18nToast(shortCutFunction, titleKey, msgKey, options = {}) {
    showToast(shortCutFunction, titleKey, msgKey, true, options);
}


/**
 * ==========================================================================================
 * Пример использования
 * ==========================================================================================
 */

// Пример 1: Простое сообщение об успехе
// showToast('success', 'Успех', 'Операция выполнена успешно!');

// Пример 2: Сообщение об ошибке с другими опциями
// showToast('error', 'Ошибка', 'Не удалось загрузить данные.', false, { duration: 9000 });

// Пример 3: Использование i18n (предполагается, что у вас есть ключи 'notifications.user.saved.title' и 'notifications.user.saved.message')
// showI18nToast('success', 'notifications.user.saved.title', 'notifications.user.saved.message');

// Пример 4: Уведомление с HTML и интерактивными кнопками
/*
const htmlMessage = `
    <div>Это сообщение содержит кнопки.</div>
    <div style="margin-top: 10px;">
        <button id="okBtn" class="btn btn-primary btn-sm">Закрыть</button>
        <button id="surpriseBtn" class="btn btn-secondary btn-sm" style="margin-left: 5px;">Сюрприз</button>
    </div>
`;
showToast('info', 'Интерактивное уведомление', htmlMessage);
*/