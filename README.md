ansj4solr
=========

solr的ansj分词插件，支持4.3以上

[ansj分词](https://github.com/ansjsun/ansj_seg)是一个对ictclas重写的智能分词java版，经过各方一定程度的检验，还是不错的。

已经有人为elasticsearch提供ansj分词接口了，我也提供一个solr的接口。

ansj的版本是0.9，作者在非中央仓库上挂了源码，但我改了一些（之后发现可以不用改），因此把ansj的两个jar直接放到项目里

可以执行mvn package打项目包 再拷贝lib里的两个ansj包（一共三个）作为solr分词所需的第三方依赖。

也可以执行mvn assembly:assembly 把zip里的三个包拿出来。

隔壁还有我为solr写的[ik分词插件](https://github.com/lgnlgn/ik4solr4.3)


源码已经完成，还没细测，文档也没整理，很快就好。
=========

