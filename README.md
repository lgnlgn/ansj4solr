ansj4solr（不再维护，未来可能删除：请去https://github.com/mlcsdev/mlcsseg  ）
=========

solr的ansj分词插件，支持4.3以上

[ansj分词](https://github.com/ansjsun/ansj_seg)是一个对ictclas重写的智能分词java版，经过各方一定程度的检验，还是不错的。

已经有人为elasticsearch提供ansj分词接口了，我也提供一个solr的接口。

ansj的版本是0.9，作者在非中央仓库上挂了源码，但我改了一些（之后发现可以不用改），因此把ansj的两个jar直接放到项目里

可以执行mvn package打项目包 再拷贝lib里的两个ansj包（一共三个）作为solr分词所需的第三方依赖。

也可以执行mvn assembly:assembly 把zip里的三个包拿出来。

默认词库有42万词语，已经打包进ansj_seg.jar中了。

隔壁还有我为solr写的[ik分词插件](https://github.com/lgnlgn/ik4solr4.3), 以及动态[停用词、同义词插件](https://github.com/lgnlgn/stop4solr4.x)包


配置如下
=========

在schema.xml中配置tokenizerfactory

     <fieldType name="text_cn" class="solr.TextField" positionIncrementGap="100">
     <analyzer type="index">
       <tokenizer class="org.ansj.solr.AnsjTokenizerFactory" conf="ansj.conf"/>
     </analyzer>
    	 <analyzer type="query">
       <tokenizer class="org.ansj.solr.AnsjTokenizerFactory" analysisType="1"/>
     </analyzer>
       </fieldType>


说明一下： 

1.

conf="ansj.conf" 这个tokenizerfactory需要的配置，里面是个properties格式的配置：

    lastupdate=123
    files=extDic.txt,aaa.txt

其中lastupdate 是一个数字，只要这次比上一次大就会触发更新操作，可以用时间戳 files是用户词库文件，以**英文逗号**隔开

conf配置只要在一个地方配置上了，整个索引使用的ansj都会启用定时更新功能，切词库是schema内共享的。这里和IK的设置是一致的。

2.

analysisType="1" 表示分词类型，1代表标准切分，不写默认是0。是索引类型切分（也就是可能多分出一点词）。

3.

rmPunc="false" 表示不去除标点符号。默认不写是true，表示去掉标点符号。
