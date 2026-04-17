import { getServiceUrl } from '../api';
import RequestService from '../httpRequest';

/**
 * GetAuthenticationtoken
 */
function getAuthToken() {
  return localStorage.getItem('token') || '';
}

/** * APIrequest * @param {Object} config - requestConfiguration * @param {string} config.url - requestURL * @param {string} config.method - Request method * @param {Object} [config.data] - requestData * @param {Object} [config.headers] - request * @param {Function} config.callback - successfulcallback * @param {Function} [config.errorCallback] - Errorcallback * @param {string} [config.errorMessage] - ErrorMessage * @param {Function} [config.retryFunction] - Function */
function makeApiRequest(config) {
  const token = getAuthToken();
  const { url, method, data, headers, callback, errorCallback, errorMessage, retryFunction } = config;

  const requestBuilder = RequestService.sendRequest()
    .url(url)
    .method(method)
    .header({
      'Authorization': `Bearer ${token}`,
      ...headers
    });

  if (data) {
    requestBuilder.data(data);
  }

  requestBuilder
    .success((res) => {
      RequestService.clearRequestTime();
      callback(res);
    })
    .fail((err) => {
      console.error(errorMessage || '操作失败', err);
      if (errorCallback) {
        errorCallback(err);
      }
    })
    .networkFail(() => {
      if (retryFunction) {
        RequestService.reAjaxFun(() => {
          retryFunction();
        });
      }
    }).send();
}

/** * Knowledge base managementAPI */
export default {
  /** * GetKnowledge base list * @param {Object} params - QueryParameter * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  getKnowledgeBaseList(params, callback, errorCallback) {
    const queryParams = new URLSearchParams({
      page: params.page,
      page_size: params.page_size,
      name: params.name || ''
    }).toString();

    makeApiRequest({
      url: `${getServiceUrl()}/datasets?${queryParams}`,
      method: 'GET',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '获取知识库列表失败',
      retryFunction: () => this.getKnowledgeBaseList(params, callback, errorCallback)
    });
  },

  /** * CreateKnowledge base * @param {Object} data - Knowledge baseData * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  createKnowledgeBase(data, callback, errorCallback) {
    console.log('createKnowledgeBase called with data:', data);
    console.log('API URL:', `${getServiceUrl()}/datasets`);

    makeApiRequest({
      url: `${getServiceUrl()}/datasets`,
      method: 'POST',
      data: data,
      headers: { 'Content-Type': 'application/json' },
      callback: (res) => {
        console.log('createKnowledgeBase success response:', res);
        callback(res);
      },
      errorCallback: (err) => {
        console.error('CreateKnowledge basefailed:', err);
        if (err.response) {
          console.error('Error response data:', err.response.data);
          console.error('Error response status:', err.response.status);
        }
        if (errorCallback) {
          errorCallback(err);
        }
      },
      errorMessage: '创建知识库失败',
      retryFunction: () => this.createKnowledgeBase(data, callback, errorCallback)
    });
  },

  /** * UpdateKnowledge base * @param {string} datasetId - Knowledge baseID * @param {Object} data - UpdateData * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  updateKnowledgeBase(datasetId, data, callback, errorCallback) {
    console.log('updateKnowledgeBase called with datasetId:', datasetId, 'data:', data);
    console.log('API URL:', `${getServiceUrl()}/datasets/${datasetId}`);

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}`,
      method: 'PUT',
      data: data,
      headers: { 'Content-Type': 'application/json' },
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '更新知识库失败',
      retryFunction: () => this.updateKnowledgeBase(datasetId, data, callback, errorCallback)
    });
  },

  /** * DeletesingleKnowledge base * @param {string} datasetId - Knowledge baseID * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  deleteKnowledgeBase(datasetId, callback, errorCallback) {
    console.log('deleteKnowledgeBase called with datasetId:', datasetId);
    console.log('API URL:', `${getServiceUrl()}/datasets/${datasetId}`);

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}`,
      method: 'DELETE',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '删除知识库失败',
      retryFunction: () => this.deleteKnowledgeBase(datasetId, callback, errorCallback)
    });
  },

  /** * batchDeleteKnowledge base * @param {string|Array} ids - Knowledge baseIDStringorArray * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  deleteKnowledgeBases(ids, callback, errorCallback) {
 // EnsureidsisFormat of String
    const idsStr = Array.isArray(ids) ? ids.join(',') : ids;

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/batch?ids=${idsStr}`,
      method: 'DELETE',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '批量删除知识库失败',
      retryFunction: () => this.deleteKnowledgeBases(ids, callback, errorCallback)
    });
  },

  /** * GetDocumentlist * @param {string} datasetId - Knowledge baseID * @param {Object} params - QueryParameter * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  getDocumentList(datasetId, params, callback, errorCallback) {
    const queryParams = new URLSearchParams({
      page: params.page,
      page_size: params.page_size,
      name: params.name || ''
    }).toString();

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/documents?${queryParams}`,
      method: 'GET',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '获取文档列表失败',
      retryFunction: () => this.getDocumentList(datasetId, params, callback, errorCallback)
    });
  },

  /** * UploadDocument * @param {string} datasetId - Knowledge baseID * @param {Object} formData - FormData * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  uploadDocument(datasetId, formData, callback, errorCallback) {
    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/documents`,
      method: 'POST',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '上传文档失败',
      retryFunction: () => this.uploadDocument(datasetId, formData, callback, errorCallback)
    });
  },

  /** * ParseDocument * @param {string} datasetId - Knowledge baseID * @param {string} documentId - DocumentID * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  parseDocument(datasetId, documentId, callback, errorCallback) {
    const requestBody = {
      document_ids: [documentId]
    };

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/chunks`,
      method: 'POST',
      data: requestBody,
      headers: { 'Content-Type': 'application/json' },
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '解析文档失败',
      retryFunction: () => this.parseDocument(datasetId, documentId, callback, errorCallback)
    });
  },

  /** * DeleteDocument * @param {string} datasetId - Knowledge baseID * @param {string} documentId - DocumentID * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  deleteDocument(datasetId, documentId, callback, errorCallback) {
    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/documents/${documentId}`,
      method: 'DELETE',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '删除文档失败',
      retryFunction: () => this.deleteDocument(datasetId, documentId, callback, errorCallback)
    });
  },

  /** * GetDocumentslicelist * @param {string} datasetId - Knowledge baseID * @param {string} documentId - DocumentID * @param {Object} params - QueryParameter * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  listChunks(datasetId, documentId, params, callback, errorCallback) {
    let queryParams = new URLSearchParams({
      page: params.page || 1,
      page_size: params.page_size || 10
    }).toString();

    // AddKeywordSearchParameter
    if (params.keywords) {
      queryParams += `&keywords=${encodeURIComponent(params.keywords)}`;
    }

    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/documents/${documentId}/chunks?${queryParams}`,
      method: 'GET',
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '获取切片列表失败',
      retryFunction: () => this.listChunks(datasetId, documentId, params, callback, errorCallback)
    });
  },

  /** * recallTest * @param {string} datasetId - Knowledge baseID * @param {Object} data - recallTestParameter * @param {Function} callback - callback * @param {Function} errorCallback - Errorcallback */
  retrievalTest(datasetId, data, callback, errorCallback) {
    makeApiRequest({
      url: `${getServiceUrl()}/datasets/${datasetId}/retrieval-test`,
      method: 'POST',
      data: data,
      headers: { 'Content-Type': 'application/json' },
      callback: callback,
      errorCallback: errorCallback,
      errorMessage: '召回测试失败',
      retryFunction: () => this.retrievalTest(datasetId, data, callback, errorCallback)
    });
  }

};