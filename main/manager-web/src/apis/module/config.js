import { getServiceUrl } from '../api';
import RequestService from '../httpRequest';

export default {
    /**
     * Upload a wizard-generated config YAML.
     *
     * @param {File}     file       YAML file chosen by the operator
     * @param {Object}   options
     * @param {Boolean}  options.dryRun  when true, validates only (no DB writes)
     * @param {String}   options.mode    'merge' (default) or 'replace'
     * @param {Function} callback   invoked with the axios response on success
     * @param {Function} failCallback invoked with the axios response on backend error
     */
    uploadConfig({ file, dryRun = false, mode = 'merge' }, callback, failCallback) {
        const formData = new FormData();
        formData.append('file', file);

        const params = new URLSearchParams({
            dryRun: String(!!dryRun),
            mode: mode === 'replace' ? 'replace' : 'merge'
        }).toString();

        RequestService.sendRequest()
            .url(`${getServiceUrl()}/config/upload?${params}`)
            .method('POST')
            .data(formData)
            .header({ 'Content-Type': 'multipart/form-data' })
            .success((res) => {
                RequestService.clearRequestTime();
                callback(res);
            })
            .fail((res) => {
                RequestService.clearRequestTime();
                if (failCallback) failCallback(res);
            })
            .networkFail((err) => {
                console.error('Config upload failed:', err);
                if (failCallback) failCallback(err);
            })
            .send();
    }
};
