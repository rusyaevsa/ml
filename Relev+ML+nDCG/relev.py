from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_distances
import json
import numpy as np
import math
import pymorphy2
from nltk.stem.snowball import RussianStemmer
from nltk.corpus import stopwords
import nltk
import string
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression


nltk.download('stopwords')
cachedStopWords = stopwords.words("russian")
nltk.download('punkt')
porter_stemmer = RussianStemmer()
morph = pymorphy2.MorphAnalyzer()
punct = string.punctuation

# преподготовка входных объектов (документов)
# удаление стоп-слов, приведение к начальной форме, отсечение окончаний и т.п.
def preparation_docs(docs):
    for i, field in enumerate(docs):
        nltk_tokens = nltk.word_tokenize(field)
        nltk_tokens = [morph.parse(word)[0].normal_form for word in nltk_tokens
                       if word not in cachedStopWords and word not in punct]
        nltk_tokens = ' '.join(porter_stemmer.stem(w_port) for w_port in nltk_tokens)
        docs[i] = nltk_tokens
    return docs


def vectorizer(data):
    docs = list()
    # считываем массив ответов на запрос (query)
    for elem in data['answer']:
        docs.extend(elem.values())
    # преподготовка данных для ответов и запроса
    docs = preparation_docs(docs)
    query = preparation_docs([data["query"]])
    # считаем метрики tf-idf для запроса и ответов
    tfidf_vectorizer = TfidfVectorizer(use_idf=True, min_df=1, token_pattern='(?u)\\b\\w+\\b')
    fitten = tfidf_vectorizer.fit(docs)
    tfidf_vectorizer_vectors = tfidf_vectorizer.transform(docs)
    query_vec = fitten.transform(query)
    # считаем косинусную близость запроса и каждого из ответов
    cosine = np.array([1 - cosine_distances(vec, query_vec)[0] for vec in tfidf_vectorizer_vectors])
    i = 2
    new_cos = list()
    # записываем в результат
    while i < len(cosine):
        new_cos.append([cosine[i - 2][0], cosine[i - 1][0], cosine[i][0]])
        i += 3
    return new_cos


# считывание файлов, расстановка весов полей документа, расстановка оценок и подсчет nDCG
def read_file(files, nDCG):
    mat = []
    # устанавливаем веса полей
    sc_title = 0.5
    sc_author = 0.4
    sc_publisher = 0.1
    for file in files:
        # считываем файл
        with open(file, "r", encoding="utf-8") as read_file:
            data = json.load(read_file)
        # считаем косинусную близость
        cosine = list(vectorizer(data))
        # пересчитываем с учетом весов
        for i, elem in enumerate(cosine):
            cosine[i].extend([sc_author * elem[0] + sc_publisher * elem[1] + sc_title * elem[2], i])
        # расставляем оценки:
        # первая 1/5 часть с самыми высокими значениями для полей title, author, publisher - оценка 5 и т.д.
        sc, i, x = 5, 0, 1
        len_mat = len(cosine)
        gr = len_mat / 5
        cosine_sort = sorted(cosine, key=lambda a_entry: a_entry[3], reverse=True)
        while i < len_mat:
            cosine_sort[i][3] = sc
            if i > gr:
                sc -= 1
                x += 1
                gr = len_mat * x / 5
            i += 1
        # подсчет nDCG
        nDCG.append(ndcg_score(cosine_sort))
        mat.extend(cosine[0:len(cosine)][:])
    return mat


# подсчет nDGC
def ndcg_score(cosine_sort):
    idcg = 0
    for i, el in enumerate(cosine_sort):
        idcg += el[3] / math.log(i + 2)
    cosine = sorted(cosine_sort, key=lambda a_entry: a_entry[4])
    dcg = 0
    for el in cosine:
        dcg += el[3] / math.log(el[4] + 2)
    ndcg = dcg / idcg
    return ndcg


# подсчет DCG
def dcg_score(mas):
    dcg = 0
    for i, el in enumerate(mas):
        dcg += el / math.log(i + 2)
    return dcg


# считываем запросы с ответами и считаем score
def score_query(nDCG):
    files = list()
    for i in range(1, 11):
        files.append("result\\query" + str(i) + ".json")
    mat = read_file(files, nDCG)
    score = list()
    for i, elem in enumerate(mat):
        mat[i] = elem[:3]
        score.append(elem[3])
    return mat, score


if __name__ == "__main__":
    nDCG = list()
    mat, score = score_query(nDCG)
    print("nDGC для каждого запроса отдельно:\n", nDCG)
    nDCG = sum(nDCG)/len(nDCG)
    print("Общий nDCG для оценки результатов поиска с помощью индекса: ", nDCG)
    # nDCG и его улучшение с помощью ML с использованием линейной регрессии
    X_train, X_test, y_train, y_test = train_test_split(mat, score, test_size=0.3)
    regressor = LinearRegression()
    # Тренеруем модель
    regressor.fit(X_train, y_train)
    # предсказываем результат с помощью модели
    y_pred = regressor.predict(X_test)
    print("Предсказанный результат: ", y_pred)

    y_sorted = sorted(zip(y_test, y_pred), key=lambda y: y[1], reverse=True)
    y_test_for_pred = [y_zip[0] for y_zip in y_sorted]
    dcg = dcg_score(y_test_for_pred)
    idcg = dcg_score(sorted(y_test, reverse=True))
    print("nDCG после применения ML: ", dcg / idcg)